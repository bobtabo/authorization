package com.authorization.usecase.gate

import com.authorization.config.Config
import com.authorization.domain.client.Repository as ClientRepository
import com.authorization.domain.gate.CacheRepository

class Interactor(
    private val clientRepo: ClientRepository,
    private val cache: CacheRepository,
    private val cfg: Config,
) {
    suspend fun issueToken(dto: IssueDto): String = TODO()
    suspend fun verify(dto: VerifyDto): Map<String, Any?> = TODO()
}
