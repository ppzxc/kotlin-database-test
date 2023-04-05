package com.nanoit.hub.couchbase.entity

import com.nanoit.hub.dto.SignUpDto
import java.time.LocalDateTime
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.couchbase.core.mapping.Document
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy
import org.springframework.data.couchbase.repository.Collection
import org.springframework.data.couchbase.repository.Scope

@Scope("members")
@Collection("users")
@Document
data class UsersEntity(
    @Id
    @GeneratedValue(strategy = GenerationStrategy.UNIQUE)
    val id: String? = null,
    val email: String,
    var password: String,
    var username: String,
    var description: String? = null,

    @Version
    val version: Long? = null,
    @CreatedBy
    var createdBy: String? = null,
    @CreatedDate
    var createdDate: LocalDateTime? = null,
    @LastModifiedBy
    var lastModifiedBy: String? = null,
    @LastModifiedDate
    var lastModifiedDate: LocalDateTime? = null,
) {

    companion object {
        fun defaults(signUpDto: SignUpDto): UsersEntity = defaults(
            signUpDto.email,
            signUpDto.password,
            signUpDto.username,
            signUpDto.description
        )

        fun defaults(
            email: String,
            password: String,
            username: String,
            description: String? = null,
        ): UsersEntity = UsersEntity(
            email = email,
            password = password,
            username = username,
            description = description,
        )
    }
}
