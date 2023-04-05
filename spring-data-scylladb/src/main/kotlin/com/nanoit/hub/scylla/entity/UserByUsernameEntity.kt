package com.nanoit.hub.scylla.entity

import java.util.UUID
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table

@Table("user_by_username")
data class UserByUsernameEntity(
    @field:Column("username")
    @field:PrimaryKey
    val username: String,
    @field:Column("password")
    var password: String,
    @field:Column("user_id")
    val userId: UUID,
)
