package com.nanoit.hub.r2dbc.entity

import java.time.LocalDateTime
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("member.authentications")
data class AuthenticationsEntity(
    @Id
    @Column("id")
    val id: Long = 0,
    @Column("account_email_verified")
    var accountEmailVerified: Boolean,
    @Column("account_non_expired")
    var accountNonExpired: Boolean,
    @Column("account_non_locked")
    var accountNonLocked: Boolean,
    @Column("credentials_non_expired")
    var credentialsNonExpired: Boolean,
    @Column("enabled")
    var enabled: Boolean,

    @Column("version")
    @Version
    val version: Long = 0,
    @Column("created_date")
    @CreatedDate
    val createdDate: LocalDateTime,
    @Column("last_modified_date")
    @LastModifiedDate
    val lastModifiedDate: LocalDateTime,
) {
    companion object {
        fun defaults(): AuthenticationsEntity = AuthenticationsEntity(
            accountEmailVerified = true,
            accountNonExpired = true,
            accountNonLocked = true,
            credentialsNonExpired = true,
            enabled = true,
            createdDate = LocalDateTime.now(),
            lastModifiedDate = LocalDateTime.now(),
        )
    }
}
