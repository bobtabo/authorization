/*
 * スタッフユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.staff

/**
 * スタッフロール更新 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class UpdateRoleDto(
    val id:         Long,
    val role:       Int,
    val executorId: Long,
)

/**
 * スタッフ削除 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class DestroyDto(
    val id:         Long,
    val executorId: Long,
)
