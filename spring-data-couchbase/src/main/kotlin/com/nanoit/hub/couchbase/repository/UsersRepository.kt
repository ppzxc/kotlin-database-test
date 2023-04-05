package com.nanoit.hub.couchbase.repository

import com.nanoit.hub.couchbase.entity.UsersEntity
import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository

interface UsersRepository : ReactiveCouchbaseRepository<UsersEntity, String>
