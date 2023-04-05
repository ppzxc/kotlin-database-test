package com.nanoit.hub.r2dbc.repository

import com.nanoit.hub.r2dbc.entity.ConfigsEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository

@Repository
interface ConfigsRepository : R2dbcRepository<ConfigsEntity, Long>
