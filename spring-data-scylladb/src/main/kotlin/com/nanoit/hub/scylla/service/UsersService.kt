package com.nanoit.hub.scylla.service

import com.nanoit.hub.dto.PutUsersDto
import com.nanoit.hub.dto.SignUpDto
import com.nanoit.hub.dto.UsersDto
import com.nanoit.hub.scylla.entity.UsersType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import reactor.core.publisher.Mono

interface UsersService {
    fun signUp(signUpDto: SignUpDto): Mono<UsersDto>

    fun getAll(localDate: LocalDate, pageable: Pageable): Mono<Slice<UsersDto>>
    fun getOne(uuid: UUID): Mono<UsersDto>
    fun putOne(uuid: UUID, putUsersDto: PutUsersDto): Mono<Void>
}
