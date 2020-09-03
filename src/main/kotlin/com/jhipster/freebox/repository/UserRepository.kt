package com.jhipster.freebox.repository

import com.jhipster.freebox.domain.Authority
import com.jhipster.freebox.domain.User
import java.time.LocalDateTime
import java.util.Optional
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.r2dbc.query.Criteria
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuples

/**
 * Spring Data R2DBC repository for the [User] entity.
 */
@Repository
interface UserRepository : R2dbcRepository<User, Long>, UserRepositoryInternal {

    @Query("SELECT * FROM jhi_user WHERE activation_key = :activationKey")
    fun findOneByActivationKey(activationKey: String): Mono<User>

    @Query("SELECT * FROM jhi_user WHERE activated = false AND activation_key IS NOT NULL AND created_date < :dateTime")
    fun findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(dateTime: LocalDateTime): Flux<User>

    @Query("SELECT * FROM jhi_user WHERE reset_key = :resetKey")
    fun findOneByResetKey(resetKey: String): Mono<User>

    @Query("SELECT * FROM jhi_user WHERE LOWER(email) = LOWER(:email)")
    fun findOneByEmailIgnoreCase(email: String): Mono<User>

    @Query("SELECT * FROM jhi_user WHERE login = :login")
    fun findOneByLogin(login: String): Mono<User>

    @Query("SELECT COUNT(DISTINCT id) FROM jhi_user WHERE login != :anonymousUser")
    fun countAllByLoginNot(anonymousUser: String): Mono<Long>

    @Query("INSERT INTO jhi_user_authority VALUES(:userId, :authority)")
    fun saveUserAuthority(userId: Long, authority: String): Mono<Void>

    @Query("DELETE FROM jhi_user_authority")
    fun deleteAllUserAuthorities(): Mono<Void>
}

interface DeleteExtended<T> {
    fun delete(user: T): Mono<Void>
}

interface UserRepositoryInternal : DeleteExtended<User> {

    fun findOneWithAuthoritiesByLogin(login: String): Mono<User>

    fun findOneWithAuthoritiesByEmailIgnoreCase(email: String): Mono<User>

    fun findAllByLoginNot(pageable: Pageable, login: String): Flux<User>
}

class UserRepositoryInternalImpl(val db: DatabaseClient, val dataAccessStrategy: ReactiveDataAccessStrategy) : UserRepositoryInternal {

    override fun findOneWithAuthoritiesByLogin(login: String): Mono<User> {
        return findOneWithAuthoritiesBy("login", login)
    }

    override fun findOneWithAuthoritiesByEmailIgnoreCase(email: String): Mono<User> {
        return findOneWithAuthoritiesBy("email", email.toLowerCase())
    }

    private fun findOneWithAuthoritiesBy(fieldName: String, fieldValue: Any): Mono<User> {
        return db.execute("SELECT * FROM jhi_user u LEFT JOIN jhi_user_authority ua ON u.id=ua.user_id WHERE u.$fieldName = :$fieldName")
            .bind(fieldName, fieldValue)
            .map { row, metadata ->
                return@map Tuples.of(
                    dataAccessStrategy.getRowMapper(User::class.java).apply(row, metadata),
                    Optional.ofNullable(row.get("authority_name", String::class.java))
                )
            }.all()
            .collectList()
            .filter { it.isNotEmpty() }
            .map { l ->
                val user = l[0]?.t1
                user?.authorities = l.filter { it.t2.isPresent }
                    .map {
                        val authority = Authority()
                        authority.name = it.t2.get()
                        authority
                    }.toMutableSet()
                user
            }
    }

    override fun findAllByLoginNot(pageable: Pageable, login: String): Flux<User> {
        return db.select().from(User::class.java)
            .matching(Criteria.where("login").not(login))
            .page(pageable)
            .`as`(User::class.java)
            .all()
    }

    override fun delete(user: User): Mono<Void> {
        return db.execute("DELETE FROM jhi_user_authority WHERE user_id = :userId")
            .bind("userId", user.id)
            .then()
            .then(db.delete()
                .from(User::class.java)
                .matching(Criteria.where("id").`is`(user.id))
                .then()
            )
    }
}
