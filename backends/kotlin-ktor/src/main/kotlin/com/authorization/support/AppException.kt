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
class AppException(val statusCode: Int, message: String) : Exception(message) {

    companion object {

        /**
         * 楽観排他ロック競合例外を生成します（HTTP 409）。
         *
         * @param message エラーメッセージ
         * @return AppException
         */
        fun conflict(message: String = "optimistic_lock"): AppException =
            AppException(409, message)

        /**
         * リソース未検出例外を生成します（HTTP 404）。
         *
         * @param message エラーメッセージ
         * @return AppException
         */
        fun notFound(message: String = "not_found"): AppException =
            AppException(404, message)
    }
}
