package com.authorization.usecase.auth

import com.authorization.domain.staff.Repository
import com.authorization.domain.staff.Staff

class Interactor(private val staffRepo: Repository) {
    suspend fun findUser(id: Long): Staff = TODO()
    suspend fun login(dto: LoginDto): Staff = TODO()
}
