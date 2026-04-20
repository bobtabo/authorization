package com.authorization.domain.notification

import java.time.LocalDateTime

data class Notification(
    val id:          Long           = 0,
    val staffId:     Long,
    val messageType: Int            = 1,
    val title:       String,
    val message:     String         = "",
    val url:         String?        = null,
    val read:        Boolean        = false,
    val createdAt:   LocalDateTime  = LocalDateTime.now(),
    val createdBy:   Long?          = null,
    val updatedAt:   LocalDateTime  = LocalDateTime.now(),
    val updatedBy:   Long?          = null,
    val deletedAt:   LocalDateTime? = null,
    val deletedBy:   Long?          = null,
    val version:     Int            = 1,
)
