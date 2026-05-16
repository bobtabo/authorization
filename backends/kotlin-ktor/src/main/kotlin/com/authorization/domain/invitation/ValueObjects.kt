/*
 * 招待ドメインの値オブジェクトモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.invitation

/**
 * 招待情報の値オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class Vo(
    val token:      String,
    val role:       Int,
    val url:        String,
    val displayUrl: String,
)
