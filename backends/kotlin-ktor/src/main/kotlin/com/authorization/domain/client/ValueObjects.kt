/*
 * クライアントドメインの値オブジェクトモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.client

import java.time.LocalDateTime

/**
 * クライアント一覧行の値オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class ListItem(
    val id:        Long,
    val name:      String,
    val status:    Int,
    val startAt:   LocalDateTime?,
    val stopAt:    LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * クライアント詳細の値オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class DetailVo(
    val id:         Long,
    val name:       String,
    val identifier: String,
    val postCode:   String,
    val pref:       String,
    val city:       String,
    val address:    String,
    val building:   String,
    val tel:        String,
    val email:      String,
    val status:     Int,
    val startAt:    LocalDateTime?,
    val stopAt:     LocalDateTime?,
    val createdAt:  LocalDateTime,
    val updatedAt:  LocalDateTime,
)

/**
 * クライアント登録結果の値オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class StoreResultVo(
    val id:          Long,
    val name:        String,
    val email:       String,
    val accessToken: String,
)
