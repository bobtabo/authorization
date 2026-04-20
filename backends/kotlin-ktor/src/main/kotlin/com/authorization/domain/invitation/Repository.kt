package com.authorization.domain.invitation

interface Repository {
    suspend fun getCurrent(): Vo?
    suspend fun issue(): Vo
    suspend fun findByToken(token: String): Vo?
}
