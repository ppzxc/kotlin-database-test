package com.nanoit.hub.scylla.entity

import java.util.UUID
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table

@Table("user_by_email")
data class UserByEmailEntity(
    @field:Column("email")
    @field:PrimaryKey
    val email: String,
    @field:Column("password")
    var password: String,
    @field:Column("user_id")
    val userId: UUID,
)
