/*
 * 通知ドメインの値オブジェクトモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.notification

/**
 * 通知ページの値オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class Page(
    val items:      List<Notification>,
    val nextCursor: String?,
)

/**
 * 通知件数の値オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class CountsVo(
    val unread: Long,
    val total:  Long,
)
