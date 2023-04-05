package com.nanoit.hub.couchbase.entity

import com.nanoit.hub.dto.UsersDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface UsersEntityMapper {

    @Mappings(
        value = [
//            Mapping(source = "id", target = "id"),
            Mapping(target = "password", constant = "****"),
        ]
    )
    fun toDto(usersEntity: UsersEntity): UsersDto
}
