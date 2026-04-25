/*
 * ゲートユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.gate

/**
 * JWT 発行 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class IssueDto(
    val accessToken: String,
    val memberId:    String,
)

/**
 * JWT 検証 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class VerifyDto(
    val identifier: String,
    val token:      String,
)
