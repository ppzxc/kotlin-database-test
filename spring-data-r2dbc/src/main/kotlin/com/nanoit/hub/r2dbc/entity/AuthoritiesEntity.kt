package com.nanoit.hub.r2dbc.entity

import java.time.LocalDateTime
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("member.authorities")
data class AuthoritiesEntity(
    @Id
    @Column("id")
    val id: Long = 0,
    @Column("users_id")
    val usersId: Long,
    @Column("roles_id")
    val rolesId: Long,

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
        fun defaults(
            usersId: Long,
            rolesId: Long,
        ): AuthoritiesEntity = AuthoritiesEntity(
            usersId = usersId,
            rolesId = rolesId,
            createdDate = LocalDateTime.now(),
            lastModifiedDate = LocalDateTime.now(),
        )
    }
}
