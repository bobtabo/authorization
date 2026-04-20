package com.authorization.usecase.invitation

import com.authorization.domain.invitation.Repository
import com.authorization.domain.invitation.Vo

class Interactor(private val repo: Repository) {
    suspend fun current(): Vo = TODO()
    suspend fun issue(): Vo = TODO()
    suspend fun findByToken(dto: FindByTokenDto): Vo = TODO()
}
