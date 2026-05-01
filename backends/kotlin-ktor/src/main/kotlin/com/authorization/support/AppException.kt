/*
 * アプリケーション例外モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.support

/**
 * HTTP ステータスコードを持つアプリケーション例外です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class AppException(val statusCode: Int, message: String) : Exception(message)
