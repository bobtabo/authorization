package com.authorization.usecase.staff

import com.authorization.domain.staff.Condition
import com.authorization.domain.staff.Repository
import com.authorization.domain.staff.Staff

class Interactor(private val repo: Repository) {
    suspend fun findByCondition(cond: Condition): List<Staff> = TODO()
    suspend fun updateRole(dto: UpdateRoleDto) = TODO()
    suspend fun restore(id: Long) = TODO()
    suspend fun destroy(dto: DestroyDto) = TODO()

    companion object {
        fun status(s: Staff): Int = if (s.deletedAt != null) 0 else 1
    }
}
