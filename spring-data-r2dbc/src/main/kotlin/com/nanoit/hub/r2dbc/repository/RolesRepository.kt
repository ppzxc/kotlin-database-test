package com.nanoit.hub.r2dbc.repository

import com.nanoit.hub.dto.Role
import com.nanoit.hub.r2dbc.entity.RolesEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface RolesRepository : R2dbcRepository<RolesEntity, Long> {
    fun findByRole(role: Role): Mono<RolesEntity>
    fun findAllByRoleIn(role: Collection<Role>): Flux<RolesEntity>
}
