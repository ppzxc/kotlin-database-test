package com.nanoit.hub.scylla.service

import com.datastax.oss.driver.api.core.uuid.Uuids
import com.nanoit.hub.dto.PutUsersDto
import com.nanoit.hub.dto.SignUpDto
import com.nanoit.hub.dto.UsersDto
import com.nanoit.hub.exceptions.NotFoundUserException
import com.nanoit.hub.scylla.entity.AuthenticationsEntity
import com.nanoit.hub.scylla.entity.Role
import com.nanoit.hub.scylla.entity.UserByEmailEntity
import com.nanoit.hub.scylla.entity.UserByUsernameEntity
import com.nanoit.hub.scylla.entity.UsersEntity
import com.nanoit.hub.scylla.entity.UsersEntityMapper
import com.nanoit.hub.scylla.entity.UsersType
import com.nanoit.hub.scylla.repository.UserByEmailRepository
import com.nanoit.hub.scylla.repository.UserByUsernameRepository
import com.nanoit.hub.scylla.repository.UsersRepository
import com.nanoit.hub.spring.utils.PasswordHelper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UsersServiceImpl(
    val usersRepository: UsersRepository,
    val userByEmailRepository: UserByEmailRepository,
    val userByUsernameRepository: UserByUsernameRepository,
    val usersEntityMapper: UsersEntityMapper,
    val reactiveCassandraTemplate: ReactiveCassandraTemplate,
) : UsersService {
    private val log = LoggerFactory.getLogger(UsersServiceImpl::class.java)

    override fun signUp(signUpDto: SignUpDto): Mono<UsersDto> {
        val userId = Uuids.random()
        val encodedPassword = PasswordHelper.encode(signUpDto.password)
        val batchOps = reactiveCassandraTemplate.batchOps()
        batchOps.insert(UserByEmailEntity(signUpDto.email, encodedPassword, userId))
        batchOps.insert(UserByUsernameEntity(signUpDto.username, encodedPassword, userId))
        val usersEntity = UsersEntity(
            userId = userId,
            registered = LocalDate.now().toString(),
            types = mutableSetOf(UsersType.values().random()),
            email = signUpDto.email,
            username = signUpDto.username,
            password = signUpDto.password,
            description = signUpDto.description,
            authorities = setOf(Role.ADMINISTRATOR),
        )
        batchOps.insert(usersEntity)
        return batchOps.execute().flatMap {
            it.rows.forEach {
                log.info("$it")
            }
            it.executionInfo.forEach {
                log.info("$it")
            }
            Mono.just(usersEntityMapper.toDto(usersEntity))
        }
    }

    override fun getAll(localDate: LocalDate, pageable: Pageable): Mono<Slice<UsersDto>> =
        usersRepository.findAllByRegisteredAfter(LocalDate.now().toString(), pageable).flatMap { slice ->
            Mono.just(SliceImpl(slice.content.map { usersEntityMapper.toDto(it) }, pageable, slice.hasNext()))
        }


    override fun getOne(uuid: UUID): Mono<UsersDto> = usersRepository.findById(uuid)
        .switchIfEmpty(Mono.error(NotFoundUserException("users.id=$uuid")))
        .flatMap { Mono.just(usersEntityMapper.toDto(it)) }

    override fun putOne(uuid: UUID, putUsersDto: PutUsersDto): Mono<Void> = usersRepository.findById(uuid)
        .switchIfEmpty(Mono.error(NotFoundUserException("users.id=$uuid")))
        .flatMap {
            it.password = putUsersDto.password
            it.username = putUsersDto.username
            it.description = putUsersDto.description
            usersRepository.save(it)
        }.then()
}
