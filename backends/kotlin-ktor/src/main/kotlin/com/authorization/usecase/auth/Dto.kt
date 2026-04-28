/*
 * 認証ユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.auth

/**
 * ログイン DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class LoginDto(
    val provider:        Int,
    val providerId:      String,
    val name:            String,
    val email:           String,
    val avatar:          String?,
    val invitationToken: String? = null,
)
