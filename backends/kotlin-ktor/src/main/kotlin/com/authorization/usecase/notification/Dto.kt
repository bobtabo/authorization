/*
 * 通知ユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.notification

/**
 * 通知一括配信 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class FanOutDto(
    val title:       String,
    val message:     String,
    val messageType: Int,
    val executorId:  Long,
    val url:         String = "",
)
