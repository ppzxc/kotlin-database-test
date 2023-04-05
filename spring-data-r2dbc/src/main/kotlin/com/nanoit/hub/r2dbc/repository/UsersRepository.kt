package com.nanoit.hub.r2dbc.repository

import com.nanoit.hub.r2dbc.entity.UsersEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UsersRepository : R2dbcRepository<UsersEntity, Long> {
    fun findByEmail(email: String): Mono<UsersEntity>
    fun existsByEmail(email: String): Mono<Boolean>
    fun findAllBy(pageable: Pageable): Flux<UsersEntity>
}
