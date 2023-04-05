package com.nanoit.hub.r2dbc.entity

import com.nanoit.hub.dto.DeviceDto
import java.time.LocalDateTime
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("member.devices")
data class DevicesEntity(
    @Id
    @Column("id")
    val id: Long = 0,
    @Column("users_id")
    val usersId: Long,

    @Column("guid")
    val guid: String,
    @Column("platform")
    val platform: String,
    @Column("name")
    val name: String,

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
        fun defaults(usersId: Long, deviceDto: DeviceDto): DevicesEntity =
            defaults(usersId, deviceDto.guid, deviceDto.platform, deviceDto.name)

        fun defaults(
            usersId: Long,
            guid: String,
            platform: String,
            name: String,
        ): DevicesEntity = DevicesEntity(
            usersId = usersId,
            guid = guid,
            platform = platform,
            name = name,
            createdDate = LocalDateTime.now(),
            lastModifiedDate = LocalDateTime.now(),
        )
    }
}
