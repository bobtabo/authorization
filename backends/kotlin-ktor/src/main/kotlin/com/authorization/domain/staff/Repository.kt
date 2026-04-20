package com.authorization.domain.staff

interface Repository {
    suspend fun findByCondition(cond: Condition): List<Staff>
    suspend fun findById(id: Long): Staff?
    suspend fun findByProvider(provider: Int, providerId: String): Staff?
    suspend fun findAllActive(): List<Staff>
    suspend fun save(s: Staff): Staff
    suspend fun updateRole(id: Long, role: Int, updatedBy: Long): Boolean
    suspend fun softDelete(id: Long, deletedBy: Long): Boolean
    suspend fun restore(id: Long): Boolean
}
