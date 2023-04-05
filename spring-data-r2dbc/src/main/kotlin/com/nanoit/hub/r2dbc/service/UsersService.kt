package com.nanoit.hub.r2dbc.service

import com.nanoit.hub.dto.AccessAndRefreshTokenDto
import com.nanoit.hub.dto.PatchUsersDto
import com.nanoit.hub.dto.PutUsersDto
import com.nanoit.hub.dto.Role
import com.nanoit.hub.dto.SignInDto
import com.nanoit.hub.dto.SignUpDto
import com.nanoit.hub.dto.UsersDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Mono

interface UsersService {
    fun signUp(signUpDto: SignUpDto): Mono<UsersDto>
    fun signIn(signInDto: SignInDto): Mono<AccessAndRefreshTokenDto>

    fun addPermission(usersId: Long, role: Role): Mono<Boolean>

    fun getUser(usersId: Long): Mono<UsersDto>
    fun getUsers(pageable: Pageable): Mono<Page<UsersDto>>
    fun putUsers(usersId: Long, putUsersDto: PutUsersDto): Mono<UsersDto>
    fun patchUsers(usersId: Long, patchUsersDto: PatchUsersDto): Mono<UsersDto>
    fun deleteUsers(usersId: Long): Mono<Void>
}
