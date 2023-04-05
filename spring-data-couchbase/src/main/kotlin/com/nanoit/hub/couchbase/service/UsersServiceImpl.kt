package com.nanoit.hub.couchbase.service

import com.nanoit.hub.couchbase.entity.UsersEntity
import com.nanoit.hub.couchbase.entity.UsersEntityMapper
import com.nanoit.hub.couchbase.repository.UsersRepository
import com.nanoit.hub.dto.SignUpDto
import com.nanoit.hub.dto.UsersDto
import com.nanoit.hub.exceptions.NotFoundUserException
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UsersServiceImpl(
    val usersRepository: UsersRepository,
    val usersEntityMapper: UsersEntityMapper,
) : UsersService {
    private val log = LoggerFactory.getLogger(UsersServiceImpl::class.java)

    override fun signUp(signUpDto: SignUpDto): Mono<UsersDto> =
        usersRepository.save(UsersEntity.defaults(signUpDto))
            .doOnNext { log.info("{}", it) }
            .map { usersEntityMapper.toDto(it) }

    override fun getAll(): Mono<List<UsersDto>> =
        usersRepository.findAll()
            .flatMap { Mono.just(usersEntityMapper.toDto(it)) }
            .collectList()

    override fun getOne(uuid: UUID): Mono<UsersDto> = usersRepository.findById(uuid.toString())
        .switchIfEmpty(Mono.error(NotFoundUserException("users.id=$uuid")))
        .flatMap { Mono.just(usersEntityMapper.toDto(it)) }
}
