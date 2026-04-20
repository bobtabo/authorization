package com.authorization.usecase.notification

data class FanOutDto(
    val title:       String,
    val message:     String,
    val messageType: Int,
    val executorId:  Long,
    val url:         String = "",
)
