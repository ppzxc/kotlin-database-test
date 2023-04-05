package com.nanoit.hub.scylla.entity

import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table

@Table("authentications_by_user")
data class AuthenticationsEntity(
    @field:PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val userId: UUID,
    @field:Column("account_email_verified")
    var accountEmailVerified: Boolean,
    @field:Column("account_non_expired")
    var accountNonExpired: Boolean,
    @field:Column("account_non_locked")
    var accountNonLocked: Boolean,
    @field:Column("credentials_non_expired")
    var credentialsNonExpired: Boolean,
    @field:Column("enabled")
    var enabled: Boolean,

    @field:Column("version")
    @field:Version
    val version: Long? = null,
    @field:Column("created_by")
    @field:CreatedBy
    var createdBy: String? = null,
    @field:Column("created_date")
    @field:CreatedDate
    var createdDate: LocalDateTime? = null,
    @field:Column("last_modified_by")
    @field:LastModifiedBy
    var lastModifiedBy: String? = null,
    @field:Column("last_modified_date")
    @field:LastModifiedDate
    var lastModifiedDate: LocalDateTime? = null,
) : Serializable {

    companion object {
        fun allOpen(userId: UUID): AuthenticationsEntity = AuthenticationsEntity(
            userId = userId,
            accountEmailVerified = true,
            accountNonExpired = true,
            accountNonLocked = true,
            credentialsNonExpired = true,
            enabled = true
        )
    }
}
