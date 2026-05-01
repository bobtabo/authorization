/*
 * クライアントドメイン列挙値モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.client

/**
 * クライアントステータスの定数です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
object ClientStatus {
    /** 無効 */
    const val INACTIVE:  Int = 1
    /** 有効 */
    const val ACTIVE:    Int = 2
    /** 停止中 */
    const val SUSPENDED: Int = 3
    /** 解約済み */
    const val CLOSED:    Int = 4
}
