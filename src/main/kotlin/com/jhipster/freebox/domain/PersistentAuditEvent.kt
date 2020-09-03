package com.jhipster.freebox.domain

import java.io.Serializable
import java.time.Instant
import javax.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceConstructor
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * Persist AuditEvent managed by the Spring Boot actuator.
 *
 * @see org.springframework.boot.actuate.audit.AuditEvent
 */
@Table("jhi_persistent_audit_event")
data class PersistentAuditEvent(

    @Id
    @Column("event_id")
    var id: Long? = null,

    @field:NotNull
    var principal: String? = null,

    @Column("event_date")
    var auditEventDate: Instant? = null,

    @Column("event_type")
    var auditEventType: String? = null,

    @Transient
    var data: MutableMap<String, String?> = mutableMapOf()

) : Serializable {

    @PersistenceConstructor
    constructor(
        id: Long?,
        principal: String?,
        auditEventDate: Instant?,
        auditEventType: String?
    ) : this(id, principal, auditEventDate, auditEventType, mutableMapOf<String, String?>())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PersistentAuditEvent) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "PersistentAuditEvent{" +
        "principal='" + principal + '\'' +
        ", auditEventDate=" + auditEventDate +
        ", auditEventType='" + auditEventType + '\'' +
        '}'

    companion object {
        private const val serialVersionUID = 1L
    }
}
