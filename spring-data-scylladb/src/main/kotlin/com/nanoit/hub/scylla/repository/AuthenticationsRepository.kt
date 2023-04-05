package com.nanoit.hub.scylla.repository

import com.nanoit.hub.scylla.entity.AuthenticationsEntity
import java.util.UUID
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthenticationsRepository : ReactiveCassandraRepository<AuthenticationsEntity, UUID>
