package com.jhipster.freebox.repository

import com.jhipster.freebox.domain.PersistentAuditEvent
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.asType
import org.springframework.data.r2dbc.query.Criteria
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.util.Pair
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Spring Data R2DBC for the [PersistentAuditEvent] entity.
 */
@Repository
interface PersistenceAuditEventRepository : R2dbcRepository<PersistentAuditEvent, Long>, PersistenceAuditEventRepositoryInternal {

    @Query("INSERT INTO jhi_persistent_audit_evt_data VALUES(:eventId, :name, :value)")
    fun savePersistenceAuditEventData(eventId: Long, name: String, value: String): Mono<Void>
}

interface PersistenceAuditEventRepositoryInternal {

    fun findByPrincipal(principal: String): Flux<PersistentAuditEvent>

    fun findAllByAuditEventDateBetween(
        fromDate: Instant,
        toDate: Instant,
        pageable: Pageable
    ): Flux<PersistentAuditEvent>

    fun findByAuditEventDateBefore(before: Instant): Flux<PersistentAuditEvent>

    fun findAllBy(pageable: Pageable): Flux<PersistentAuditEvent>

    fun countByAuditEventDateBetween(fromDate: Instant, toDate: Instant): Mono<Long>
}

class PersistenceAuditEventRepositoryInternalImpl(val databaseClient: DatabaseClient) : PersistenceAuditEventRepositoryInternal {

    override fun findByPrincipal(principal: String) =
        findAllByCriteria(Criteria.where("principal").`is`(principal))

    override fun findAllByAuditEventDateBetween(fromDate: Instant, toDate: Instant, pageable: Pageable): Flux<PersistentAuditEvent> {
        // LocalDateTime seems to be the only type that is supported across all drivers atm
        // See https://github.com/r2dbc/r2dbc-h2/pull/139 https://github.com/mirromutth/r2dbc-mysql/issues/105
        val fromDateLocal = LocalDateTime.ofInstant(fromDate, ZoneOffset.UTC)
        val toDateLocal = LocalDateTime.ofInstant(toDate, ZoneOffset.UTC)
        val criteria = Criteria
            .where("event_date").greaterThan(fromDateLocal)
            .and("event_date").lessThan(toDateLocal)
        return findAllFromSpec(select().matching(criteria).page(pageable))
    }

    override fun findByAuditEventDateBefore(before: Instant): Flux<PersistentAuditEvent> {
        // LocalDateTime seems to be the only type that is supported across all drivers atm
        // See https://github.com/r2dbc/r2dbc-h2/pull/139 https://github.com/mirromutth/r2dbc-mysql/issues/105
        val beforeLocal = LocalDateTime.ofInstant(before, ZoneOffset.UTC)
        return findAllByCriteria(Criteria.where("event_date").lessThan(beforeLocal))
    }

    override fun findAllBy(pageable: Pageable): Flux<PersistentAuditEvent> =
        findAllFromSpec(select().page(pageable))

    override fun countByAuditEventDateBetween(fromDate: Instant, toDate: Instant): Mono<Long> {
        // LocalDateTime seems to be the only type that is supported across all drivers atm
        // See https://github.com/r2dbc/r2dbc-h2/pull/139 https://github.com/mirromutth/r2dbc-mysql/issues/105
        val fromDateLocal = LocalDateTime.ofInstant(fromDate, ZoneOffset.UTC)
        val toDateLocal = LocalDateTime.ofInstant(toDate, ZoneOffset.UTC)
        return databaseClient.execute("SELECT COUNT(DISTINCT event_id) FROM jhi_persistent_audit_event " +
            "WHERE event_date > :fromDate AND event_date < :toDate")
            .bind("fromDate", fromDateLocal)
            .bind("toDate", toDateLocal)
            .asType<Long>()
            .fetch()
            .one()
    }

    private fun findAllByCriteria(criteria: Criteria) =
        findAllFromSpec(select().matching(criteria))

    private fun select() = databaseClient.select().from(PersistentAuditEvent::class.java)

    private fun findAllFromSpec(spec: DatabaseClient.TypedSelectSpec<PersistentAuditEvent>): Flux<PersistentAuditEvent> =
        spec.`as`(PersistentAuditEvent::class.java).all()
        .flatMap { event ->
                findAllEventData(event.id!!)
                    .map {
                        event.data = it
                        event
                    }
        }

    private fun findAllEventData(id: Long): Mono<MutableMap<String, String?>> = databaseClient.select().from("jhi_persistent_audit_evt_data")
        .project("name", "value")
        .matching(Criteria.where("event_id").`is`(id))
        .map { row ->
            Pair.of(row.get("name", String::class.java) ?: "", row.get("value", String::class.java) ?: "")
        }
        .all()
        .collectMap(Pair<String, String?>::getFirst, Pair<String, String?>::getSecond)
}
