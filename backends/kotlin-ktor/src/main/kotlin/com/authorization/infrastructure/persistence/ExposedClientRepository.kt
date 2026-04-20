package com.authorization.infrastructure.persistence

import com.authorization.domain.client.Client
import com.authorization.domain.client.Condition
import com.authorization.domain.client.Repository
import org.jetbrains.exposed.sql.Database

class ExposedClientRepository(private val db: Database) : Repository {
    override suspend fun findByCondition(cond: Condition): List<Client> = TODO()
    override suspend fun findById(id: Long): Client? = TODO()
    override suspend fun findByAccessToken(token: String): Client? = TODO()
    override suspend fun findByIdentifier(identifier: String): Client? = TODO()
    override suspend fun save(c: Client): Client = TODO()
    override suspend fun softDelete(id: Long, deletedBy: Long) = TODO()
}
