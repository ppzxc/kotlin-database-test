package com.nanoit.hub.r2dbc.repository

import com.nanoit.hub.r2dbc.entity.AuthoritiesEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface AuthoritiesRepository : R2dbcRepository<AuthoritiesEntity, Long> {
    fun findAllByUsersId(usersId: Long): Flux<AuthoritiesEntity>
    fun deleteByUsersId(usersId: Long): Mono<Void>
}
