package com.nanoit.hub.r2dbc.entity

import com.nanoit.hub.dto.ConfigsDto
import java.time.LocalDateTime
import org.mapstruct.Mapper
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("member.configs")
data class ConfigsEntity(
    @Id
    @Column("id")
    val id: Long = 0,

    @Column("client")
    var client: String?,

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
        fun defaults(client: String? = null): ConfigsEntity = ConfigsEntity(
            client = client,
            createdDate = LocalDateTime.now(),
            lastModifiedDate = LocalDateTime.now(),
        )
    }
}

@Mapper(componentModel = "spring")
interface ConfigsEntityMapper {

    fun toDto(configsEntity: ConfigsEntity): ConfigsDto
}
