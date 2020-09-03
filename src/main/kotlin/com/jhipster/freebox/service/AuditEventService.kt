package com.jhipster.freebox.service

import com.jhipster.freebox.config.audit.AuditEventConverter
import com.jhipster.freebox.domain.PersistentAuditEvent
import com.jhipster.freebox.repository.PersistenceAuditEventRepository
import io.github.jhipster.config.JHipsterProperties
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.actuate.security.AuthenticationAuditListener.AUTHENTICATION_FAILURE
import org.springframework.boot.actuate.security.AuthenticationAuditListener.AUTHENTICATION_SUCCESS
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.util.function.Tuples

/**
 * Service for managing audit events.
 *
 * This is the default implementation to support SpringBoot Actuator `AuditEventRepository`.
 */
@Service
class AuditEventService(
    private val persistenceAuditEventRepository: PersistenceAuditEventRepository,
    private val auditEventConverter: AuditEventConverter,
    private val jHipsterProperties: JHipsterProperties
) {

    /**
    * Should be the same as in Liquibase migration.
    */
    private val EVENT_DATA_COLUMN_MAX_LENGTH = 255

    private val log = LoggerFactory.getLogger(javaClass)

    /**
    * Old audit events should be automatically deleted after 30 days.
    *
    * This is scheduled to get fired at 12:00 (am).
    */
    @Scheduled(cron = "0 0 12 * * ?")
    fun removeOldAuditEvents() {
        removeOldAuditEventsReactively().block()
    }

    @Transactional
    fun removeOldAuditEventsReactively(): Mono<Void> {
        return persistenceAuditEventRepository
            .findByAuditEventDateBefore(Instant.now().minus(jHipsterProperties.auditEvents.retentionPeriod.toLong(), ChronoUnit.DAYS))
            .flatMap {
                log.debug("Deleting audit data $it")
                persistenceAuditEventRepository.delete(it)
            }.then()
    }

    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Flux<AuditEvent> =
        persistenceAuditEventRepository.findAllBy(pageable)
            .map { auditEventConverter.convertToAuditEvent(it) }

    @Transactional(readOnly = true)
    fun findByDates(fromDate: Instant, toDate: Instant, pageable: Pageable): Flux<AuditEvent> =
        persistenceAuditEventRepository.findAllByAuditEventDateBetween(fromDate, toDate, pageable)
            .map { auditEventConverter.convertToAuditEvent(it) }

    @Transactional(readOnly = true)
    fun find(id: Long): Mono<AuditEvent> =
        persistenceAuditEventRepository.findById(id)
            .map { auditEventConverter.convertToAuditEvent(it) }

    @Transactional(readOnly = true)
    fun count(): Mono<Long> {
        return persistenceAuditEventRepository.count()
    }

    @Transactional(readOnly = true)
    fun countByDates(fromDate: Instant, toDate: Instant): Mono<Long> {
        return persistenceAuditEventRepository.countByAuditEventDateBetween(fromDate, toDate)
    }

    @Transactional(readOnly = true)
    fun saveAuthenticationSuccess(login: String): Mono<PersistentAuditEvent> {
        val persistentAuditEvent = PersistentAuditEvent()
        persistentAuditEvent.principal = login
        persistentAuditEvent.auditEventType = AUTHENTICATION_SUCCESS
        persistentAuditEvent.auditEventDate = Instant.now()
        return persistenceAuditEventRepository.save(persistentAuditEvent)
    }

    @Transactional(readOnly = true)
    fun saveAuthenticationError(login: String, e: Throwable): Mono<PersistentAuditEvent> {
        val persistentAuditEvent = PersistentAuditEvent()
        persistentAuditEvent.principal = login
        persistentAuditEvent.auditEventType = AUTHENTICATION_FAILURE
        persistentAuditEvent.auditEventDate = Instant.now()
        val eventData = mutableMapOf<String, String?>()
        eventData["type"] = e::class.java.name
        eventData["message"] = e.message
        persistentAuditEvent.data = truncate(eventData)
        return persistenceAuditEventRepository.save(persistentAuditEvent)
            .flatMapMany { event ->
                event.data.entries
                        .map { Tuples.of(event!!, it.key, it.value!!) }
                    .toFlux()
            }
            .flatMap { t ->
                t.t1.id?.let {
                    persistenceAuditEventRepository
                        .savePersistenceAuditEventData(it, t.t2, t.t3)
                        .thenReturn(t.t1)
                }
            }.last()
    }

    /**
     * Truncate event data that might exceed column length.
     */
    private fun truncate(data: MutableMap<String, String?>): MutableMap<String, String?> {
        val results = mutableMapOf<String, String?>()
        data.entries.forEach {
            var value = it.value
            if (value != null) {
                val length = value.length
                if (length > EVENT_DATA_COLUMN_MAX_LENGTH) {
                    value = value.substring(0, EVENT_DATA_COLUMN_MAX_LENGTH)
                    log.warn("Event data for ${it.key} too long ($length) has been truncated to $EVENT_DATA_COLUMN_MAX_LENGTH. Consider increasing column width.")
                }
            }
            results[it.key] = value
        }
        return results
    }
}
