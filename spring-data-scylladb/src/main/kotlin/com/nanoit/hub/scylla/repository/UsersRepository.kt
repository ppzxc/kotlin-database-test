package com.nanoit.hub.scylla.repository

import com.nanoit.hub.scylla.entity.UsersEntity
import com.nanoit.hub.scylla.entity.UsersType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UsersRepository : ReactiveCassandraRepository<UsersEntity, UUID> {
    fun findAllByRegisteredAfter(localDate: String, pageable: Pageable): Mono<Slice<UsersEntity>>
}
