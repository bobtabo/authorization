/*
 * クライアントドメイン検索条件モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.client

import java.time.LocalDateTime

/**
 * クライアント検索条件です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class Condition(
    val keyword:   String?          = null,
    val startFrom: LocalDateTime?   = null,
    val startTo:   LocalDateTime?   = null,
    val statuses:  List<Int>        = emptyList(),
    val offset:    Int              = 0,
    val limit:     Int              = 10,
    val sort:      String?          = null,
    val sortType:  String?          = null,
)
