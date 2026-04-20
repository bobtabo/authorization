package com.authorization.domain.client

interface Repository {
    suspend fun findByCondition(cond: Condition): List<Client>
    suspend fun findById(id: Long): Client?
    suspend fun findByAccessToken(token: String): Client?
    suspend fun findByIdentifier(identifier: String): Client?
    suspend fun save(c: Client): Client
    suspend fun softDelete(id: Long, deletedBy: Long)
}
