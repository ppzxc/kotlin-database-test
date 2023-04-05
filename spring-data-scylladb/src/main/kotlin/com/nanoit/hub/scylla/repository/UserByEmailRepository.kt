package com.nanoit.hub.scylla.repository

import com.nanoit.hub.scylla.entity.UserByEmailEntity
import java.util.UUID
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface UserByEmailRepository : ReactiveCassandraRepository<UserByEmailEntity, UUID>
