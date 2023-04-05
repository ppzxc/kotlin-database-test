package com.nanoit.hub.r2dbc.entity

import com.nanoit.hub.dto.UsersDto
import java.time.LocalDateTime
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("member.users")
data class UsersEntity(
    @Id
    @Column("id")
    val id: Long = 0,
    @Column("authentications_id")
    val authenticationsId: Long,
    @Column("configs_id")
    val configsId: Long,
    @Column("email")
    val email: String,
    @Column("password")
    var password: String,
    @Column("username")
    var username: String,
    @Column("description")
    var description: String? = null,

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
            authenticationsId: Long,
            configsId: Long,
            email: String,
            password: String,
            username: String,
            description: String? = null,
        ): UsersEntity = UsersEntity(
            authenticationsId = authenticationsId,
            configsId = configsId,
            email = email,
            password = password,
            username = username,
            description = description,
            createdDate = LocalDateTime.now(),
            lastModifiedDate = LocalDateTime.now(),
        )
    }
}

@Mapper(componentModel = "spring")
interface UsersEntityMapper {

    @Mappings(
        value = [
            Mapping(target = "password", constant = "****"),
        ]
    )
    fun toDto(usersEntity: UsersEntity): UsersDto
}
