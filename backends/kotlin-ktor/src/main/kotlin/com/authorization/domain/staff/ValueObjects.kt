package com.authorization.domain.staff

import java.time.LocalDateTime

data class ListItem(
    val id:        Long,
    val name:      String,
    val email:     String,
    val role:      Int,
    val status:    Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class Vo(
    val id:     Long,
    val name:   String,
    val avatar: String?,
    val role:   Int,
)
