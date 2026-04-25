/*
 * 招待ユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.invitation

/**
 * 招待トークン検索 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class FindByTokenDto(
    val token: String,
)
