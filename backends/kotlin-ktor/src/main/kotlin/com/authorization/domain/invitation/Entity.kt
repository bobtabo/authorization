package com.authorization.domain.invitation

import java.time.LocalDateTime

data class Invitation(
    val id:        Int            = 0,
    val token:     String,
    val createdAt: LocalDateTime  = LocalDateTime.now(),
    val createdBy: Long?          = null,
    val updatedAt: LocalDateTime  = LocalDateTime.now(),
    val updatedBy: Long?          = null,
    val deletedAt: LocalDateTime? = null,
    val deletedBy: Long?          = null,
    val version:   Int            = 0,
)
