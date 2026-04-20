package com.authorization.domain.gate

data class IssueVo(val token: String)

data class VerifyVo(val claims: Map<String, Any?>)

interface CacheRepository {
    suspend fun getJwt(identifier: String, memberId: String): String?
    suspend fun putJwt(identifier: String, memberId: String, token: String, ttl: Long)
}
