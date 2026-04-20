package com.authorization.usecase.client

import com.authorization.domain.client.Client
import com.authorization.domain.client.Repository

class Interactor(private val repo: Repository) {
    suspend fun findByCondition(dto: ListConditionDto): List<Client> = TODO()
    suspend fun findById(id: Long): Client = TODO()
    suspend fun store(dto: StoreDto): Client = TODO()
    suspend fun update(dto: UpdateDto): Client = TODO()
    suspend fun destroy(id: Long, executorId: Long) = TODO()
    suspend fun findByAccessToken(token: String): Client? = TODO()
    suspend fun findByIdentifier(identifier: String): Client? = TODO()
}
