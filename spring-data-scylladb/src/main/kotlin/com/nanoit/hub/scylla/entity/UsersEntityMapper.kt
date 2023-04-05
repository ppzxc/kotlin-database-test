package com.nanoit.hub.scylla.entity

import com.nanoit.hub.dto.UsersDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface UsersEntityMapper {

    @Mappings(
        value = [
            Mapping(target = "id", expression = "java(usersEntity.getUserId().toString().replace(\"-\", \"\"))"),
            Mapping(target = "password", constant = "****"),
        ]
    )
    fun toDto(usersEntity: UsersEntity): UsersDto
}
