/*
 * スタッフドメインの値オブジェクトモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.staff

import java.time.LocalDateTime

/**
 * スタッフ一覧行の値オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class ListItem(
    val id:        Long,
    val name:      String,
    val email:     String,
    val role:      Int,
    val status:    Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * スタッフ詳細の値オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class Vo(
    val id:     Long,
    val name:   String,
    val avatar: String?,
    val role:   Int,
)
