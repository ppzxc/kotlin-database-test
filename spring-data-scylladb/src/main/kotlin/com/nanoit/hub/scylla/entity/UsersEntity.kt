package com.nanoit.hub.scylla.entity

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

@Table("users")
data class UsersEntity(
    @field:PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val userId: UUID,
    @field:PrimaryKeyColumn(name = "registered", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val registered: String,

    @field:Column("types")
    val types: Set<UsersType>,
    @field:Column("email")
    val email: String,
    @field:Column("username")
    var username: String,
    @field:Column("password")
    var password: String,
    @field:Column("description")
    var description: String? = null,
    @field:Column("authorities")
    val authorities: Set<Role>,

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
)
