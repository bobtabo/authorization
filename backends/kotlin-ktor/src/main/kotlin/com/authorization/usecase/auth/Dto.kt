package com.authorization.usecase.auth

data class LoginDto(
    val provider:   Int,
    val providerId: String,
    val name:       String,
    val email:      String,
    val avatar:     String?,
)
