package com.nanoit.hub.r2dbc.repository

import com.nanoit.hub.r2dbc.entity.DevicesEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface DevicesRepository : R2dbcRepository<DevicesEntity, Long> {
    fun findByUsersIdAndGuid(usersId: Long, guid: String): Mono<DevicesEntity>
    fun deleteByUsersId(usersId: Long): Mono<Void>
}
