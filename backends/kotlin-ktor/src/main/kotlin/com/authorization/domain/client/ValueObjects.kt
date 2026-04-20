package com.authorization.domain.client

import java.time.LocalDateTime

data class ListItem(
    val id:        Long,
    val name:      String,
    val status:    Int,
    val startAt:   LocalDateTime?,
    val stopAt:    LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class DetailVo(
    val id:         Long,
    val name:       String,
    val identifier: String,
    val postCode:   String,
    val pref:       String,
    val city:       String,
    val address:    String,
    val building:   String,
    val tel:        String,
    val email:      String,
    val status:     Int,
    val startAt:    LocalDateTime?,
    val stopAt:     LocalDateTime?,
    val createdAt:  LocalDateTime,
    val updatedAt:  LocalDateTime,
)
