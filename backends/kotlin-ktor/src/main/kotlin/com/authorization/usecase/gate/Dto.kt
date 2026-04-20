package com.authorization.usecase.gate

data class IssueDto(
    val accessToken: String,
    val memberId:    String,
)

data class VerifyDto(
    val identifier: String,
    val token:      String,
)
