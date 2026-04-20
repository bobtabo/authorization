package com.authorization.infrastructure.persistence

import com.authorization.domain.staff.Condition
import com.authorization.domain.staff.Repository
import com.authorization.domain.staff.Staff
import org.jetbrains.exposed.sql.Database

class ExposedStaffRepository(private val db: Database) : Repository {
    override suspend fun findByCondition(cond: Condition): List<Staff> = TODO()
    override suspend fun findById(id: Long): Staff? = TODO()
    override suspend fun findByProvider(provider: Int, providerId: String): Staff? = TODO()
    override suspend fun findAllActive(): List<Staff> = TODO()
    override suspend fun save(s: Staff): Staff = TODO()
    override suspend fun updateRole(id: Long, role: Int, updatedBy: Long): Boolean = TODO()
    override suspend fun softDelete(id: Long, deletedBy: Long): Boolean = TODO()
    override suspend fun restore(id: Long): Boolean = TODO()
}
