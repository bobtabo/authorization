package com.authorization.usecase.staff

data class UpdateRoleDto(
    val id:         Long,
    val role:       Int,
    val executorId: Long,
)

data class DestroyDto(
    val id:         Long,
    val executorId: Long,
)
