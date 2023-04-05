package com.nanoit.hub.r2dbc.service

import com.nanoit.hub.dto.AccessAndRefreshTokenDto
import com.nanoit.hub.dto.DeviceDto
import com.nanoit.hub.dto.MessengerPrincipalImpl
import com.nanoit.hub.dto.PatchUsersDto
import com.nanoit.hub.dto.PutUsersDto
import com.nanoit.hub.dto.Role
import com.nanoit.hub.dto.SignInDto
import com.nanoit.hub.dto.SignUpDto
import com.nanoit.hub.dto.UsersDto
import com.nanoit.hub.exceptions.DeleteException
import com.nanoit.hub.exceptions.DuplicateEmailException
import com.nanoit.hub.exceptions.InvalidCredentialsException
import com.nanoit.hub.exceptions.MessengerErrorCodeException
import com.nanoit.hub.exceptions.NotFoundUserException
import com.nanoit.hub.exceptions.PatchException
import com.nanoit.hub.exceptions.PutException
import com.nanoit.hub.exceptions.SignInFailedException
import com.nanoit.hub.exceptions.SignUpFailedException
import com.nanoit.hub.r2dbc.entity.AuthenticationsEntity
import com.nanoit.hub.r2dbc.entity.AuthoritiesEntity
import com.nanoit.hub.r2dbc.entity.ConfigsEntity
import com.nanoit.hub.r2dbc.entity.UsersEntity
import com.nanoit.hub.r2dbc.entity.UsersEntityMapper
import com.nanoit.hub.r2dbc.repository.AuthenticationsRepository
import com.nanoit.hub.r2dbc.repository.AuthoritiesRepository
import com.nanoit.hub.r2dbc.repository.ConfigsRepository
import com.nanoit.hub.r2dbc.repository.DevicesRepository
import com.nanoit.hub.r2dbc.repository.RolesRepository
import com.nanoit.hub.r2dbc.repository.UsersRepository
import com.nanoit.hub.r2dbc.utils.RetryStrategy
import com.nanoit.hub.utility.spring.PasswordHelper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class UsersServiceImpl(
    val usersRepository: UsersRepository,
    val authenticationsRepository: AuthenticationsRepository,
    val authoritiesRepository: AuthoritiesRepository,
    val rolesRepository: RolesRepository,
    val devicesRepository: DevicesRepository,
    val configsRepository: ConfigsRepository,
    val usersEntityMapper: UsersEntityMapper,
    val tokenService: TokenService,
) : UsersService {
    private val log = LoggerFactory.getLogger(UsersServiceImpl::class.java)

    @Transactional(rollbackFor = [Throwable::class])
    override fun signUp(signUpDto: SignUpDto): Mono<UsersDto> =
        validation(signUpDto)
            .flatMap { createUsers(it) }
            .flatMap { createAuthorities(it) }
            .flatMap { Mono.just(usersEntityMapper.toDto(it)) }
            .doOnNext { log.info("[#SIGN_UP#] id={} email={}", it.id, it.email) }
            .onErrorMap { signUpErrorMapping(it) }

    private fun validation(signUpDto: SignUpDto): Mono<SignUpDto> =
        usersRepository.existsByEmail(signUpDto.email).flatMap {
            when (it) {
                true -> Mono.error(DuplicateEmailException("duplicate email=${signUpDto.email}"))
                false -> Mono.just(signUpDto)
            }
        }

    private fun createUsers(signUp: SignUpDto): Mono<UsersEntity> =
        Mono.zip(
            authenticationsRepository.save(AuthenticationsEntity.defaults()),
            configsRepository.save(ConfigsEntity.defaults())
        ).flatMap {
            usersRepository.save(
                UsersEntity.defaults(
                    authenticationsId = it.t1.id,
                    configsId = it.t2.id,
                    email = signUp.email,
                    password = PasswordHelper.encode(signUp.password),
                    username = signUp.username,
                    description = signUp.description
                )
            )
        }

    private fun createAuthorities(usersEntity: UsersEntity): Mono<UsersEntity> =
        rolesRepository.findAllByRoleIn(listOf(Role.CLIENT_CELL_PHONE, Role.CLIENT_APP, Role.CLIENT_WEB)).flatMap {
            authoritiesRepository.save(AuthoritiesEntity.defaults(usersEntity.id, it.id))
        }.collectList().flatMap {
            Mono.just(usersEntity)
        }

    private fun signUpErrorMapping(it: Throwable): Throwable {
        log.error("[#SIGN_UP#]", it)
        return when (it) {
            is MessengerErrorCodeException -> SignUpFailedException(it)
            else -> SignUpFailedException("unknown")
        }
    }

    @Transactional(rollbackFor = [Throwable::class])
    override fun signIn(signInDto: SignInDto): Mono<AccessAndRefreshTokenDto> =
        usersRepository.findByEmail(signInDto.email)
            .switchIfEmpty(Mono.error(NotFoundUserException("email=${signInDto.email}")))
            .flatMap { passwordCompare(signInDto, it) }
            .doOnNext { log.info("[#SIGN_IN#] email={}", signInDto.email) }
            .onErrorMap { signInErrorMapping(it) }

    private fun passwordCompare(signInDto: SignInDto, usersEntity: UsersEntity): Mono<AccessAndRefreshTokenDto> =
        when (PasswordHelper.compare(signInDto.password, usersEntity.password)) {
            false -> Mono.error(InvalidCredentialsException())
            true -> createCredentials(usersEntity, signInDto.device)
        }

    private fun createCredentials(usersEntity: UsersEntity, devicesDto: DeviceDto): Mono<AccessAndRefreshTokenDto> =
        authoritiesRepository.findAllByUsersId(usersEntity.id).collectList()
            .flatMap { authoritiesEntities -> Mono.just(authoritiesEntities.map { it.rolesId }) }
            .flatMap { rolesRepository.findAllById(it).collectList() }
            .flatMap { rolesEntities -> Mono.just(rolesEntities.map { it.role.name }) }
            .flatMap { tokenService.issueSignInToken(MessengerPrincipalImpl(usersEntity.id, it), devicesDto) }

    private fun signInErrorMapping(it: Throwable): Throwable {
        log.error("[#SIGN_IN#]", it)
        return when (it) {
            is MessengerErrorCodeException -> SignInFailedException(it)
            else -> SignInFailedException("unknown")
        }
    }

    override fun getUser(usersId: Long): Mono<UsersDto> =
        usersRepository.findById(usersId)
            .switchIfEmpty(Mono.error(NotFoundUserException("usersId=$usersId")))
            .flatMap { Mono.just(usersEntityMapper.toDto(it)) }
            .onErrorMap {
                when (it) {
                    it as MessengerErrorCodeException -> it
                    else -> NotFoundUserException("usersId=$usersId")
                }
            }

    override fun getUsers(pageable: Pageable): Mono<Page<UsersDto>> =
        usersRepository.findAllBy(pageable)
            .switchIfEmpty(Mono.error(NotFoundUserException("users zero")))
            .flatMap { Mono.just(usersEntityMapper.toDto(it)) }
            .collectList()
            .zipWith(usersRepository.count())
            .flatMap { Mono.just(PageImpl(it.t1, pageable, it.t2)) }

    override fun addPermission(usersId: Long, role: Role): Mono<Boolean> =
        rolesRepository.findByRole(role)
            .flatMap { authoritiesRepository.save(AuthoritiesEntity.defaults(usersId, it.id)) }
            .flatMap { Mono.just(true) }

    override fun putUsers(usersId: Long, putUsersDto: PutUsersDto): Mono<UsersDto> =
        usersRepository.findById(usersId)
            .switchIfEmpty(Mono.error(NotFoundUserException("usersId=$usersId")))
            .flatMap { usersEntity ->
                usersEntity.password = PasswordHelper.encode(putUsersDto.password)
                usersEntity.username = putUsersDto.username
                usersEntity.description = putUsersDto.description
                usersRepository.save(usersEntity)
            }.flatMap { Mono.just(usersEntityMapper.toDto(it)) }
            .onErrorMap {
                when (it) {
                    it as MessengerErrorCodeException -> it
                    else -> PutException("PUT usersId=$usersId")
                }
            }

    override fun patchUsers(usersId: Long, patchUsersDto: PatchUsersDto): Mono<UsersDto> =
        usersRepository.findById(usersId)
            .switchIfEmpty(Mono.error(NotFoundUserException("usersId=$usersId")))
            .flatMap { usersEntity ->
                patchUsersDto.password?.let {
                    usersEntity.password = PasswordHelper.encode(it)
                }
                patchUsersDto.username?.let {
                    usersEntity.username = it
                }
                patchUsersDto.description?.let {
                    usersEntity.description = it
                }
                usersRepository.save(usersEntity)
            }.flatMap { Mono.just(usersEntityMapper.toDto(it)) }
            .onErrorMap {
                when (it) {
                    it as MessengerErrorCodeException -> it
                    else -> PatchException("PATCH usersId=$usersId")
                }
            }

    @Transactional(rollbackFor = [Throwable::class])
    override fun deleteUsers(usersId: Long): Mono<Void> =
        usersRepository.findById(usersId)
            .switchIfEmpty(Mono.error(NotFoundUserException("usersId=$usersId")))
            .flatMap { deleteMember(it) }
            .onErrorMap {
                when (it) {
                    it as MessengerErrorCodeException -> it
                    else -> DeleteException("DELETE usersId=$usersId")
                }
            }

    private fun deleteMember(usersEntity: UsersEntity): Mono<Void> =
        authoritiesRepository.deleteByUsersId(usersEntity.id)
            .then(devicesRepository.deleteByUsersId(usersEntity.id))
            .then(usersRepository.deleteById(usersEntity.id))
            .then(authenticationsRepository.deleteById(usersEntity.authenticationsId))
}
