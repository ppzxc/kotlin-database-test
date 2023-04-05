package com.nanoit.hub.couchbase.service

import com.nanoit.hub.dto.SignUpDto
import com.nanoit.hub.dto.UsersDto
import java.util.UUID
import reactor.core.publisher.Mono

interface UsersService {
    fun signUp(signUpDto: SignUpDto): Mono<UsersDto>
    fun getAll(): Mono<List<UsersDto>>
    fun getOne(uuid: UUID): Mono<UsersDto>
}
