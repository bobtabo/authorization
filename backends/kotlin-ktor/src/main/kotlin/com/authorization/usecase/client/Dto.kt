/*
 * クライアントユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.client

/**
 * クライアント登録 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class StoreDto(
    val name:       String,
    val postCode:   String = "",
    val pref:       String = "",
    val city:       String = "",
    val address:    String = "",
    val building:   String = "",
    val tel:        String = "",
    val email:      String = "",
    val executorId: Long   = 0,
)

/**
 * クライアント更新 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class UpdateDto(
    val id:         Long,
    val name:       String?  = null,
    val postCode:   String?  = null,
    val pref:       String?  = null,
    val city:       String?  = null,
    val address:    String?  = null,
    val building:   String?  = null,
    val tel:        String?  = null,
    val email:      String?  = null,
    val status:     Int?     = null,
    val executorId: Long     = 0,
    val version:    Int      = 0,
)

/**
 * クライアント一覧取得条件 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class ListConditionDto(
    val keyword:   String?    = null,
    val startFrom: String?    = null,
    val startTo:   String?    = null,
    val statuses:  List<Int>  = emptyList(),
    val offset:    Int         = 0,
    val limit:     Int         = 10,
    val sort:      String?    = null,
    val sortType:  String?    = null,
)

/**
 * スマホ連携 QR コード取得 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class QrDto(val identifier: String)

/**
 * スマホ連携クライアント情報取得 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class InfoDto(val identifier: String)

/**
 * スマホ連携利用開始 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class StartDto(val identifier: String)

/**
 * スマホ連携利用停止 DTO です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class StopDto(val identifier: String)
