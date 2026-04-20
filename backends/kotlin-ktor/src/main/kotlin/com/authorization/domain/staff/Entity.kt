package com.authorization.domain.staff

import java.time.LocalDateTime

data class Staff(
    val id:          Long           = 0,
    val name:        String,
    val email:       String,
    val provider:    Int,
    val providerId:  String,
    val avatar:      String?        = null,
    val role:        Int            = StaffRole.MEMBER,
    val lastLoginAt: LocalDateTime? = null,
    val createdAt:   LocalDateTime  = LocalDateTime.now(),
    val createdBy:   Long?          = null,
    val updatedAt:   LocalDateTime  = LocalDateTime.now(),
    val updatedBy:   Long?          = null,
    val deletedAt:   LocalDateTime? = null,
    val deletedBy:   Long?          = null,
    val version:     Int            = 0,
)
