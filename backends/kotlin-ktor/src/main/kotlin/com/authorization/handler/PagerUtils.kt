/*
 * ページャー構築ユーティリティ。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.handler

import kotlinx.serialization.json.*

internal const val DEFAULT_PAGE_COUNT = 5

internal fun buildPager(count: Int, limit: Int, offset: Int, recordCount: Int): JsonObject {
    val effectiveLimit = if (limit <= 0) 10 else limit
    val pageCount = maxOf(1, kotlin.math.ceil(count.toDouble() / effectiveLimit).toInt())
    val page = (kotlin.math.ceil(offset.toDouble() / effectiveLimit).toInt()) + 1
    val startPage = maxOf(1, page - (DEFAULT_PAGE_COUNT - 1))
    val endPage = minOf(pageCount, startPage + (DEFAULT_PAGE_COUNT - 1))
    val firstRecordCount = if (count == 0 || recordCount == 0) 0 else offset + 1
    val lastRecordCount = if (recordCount == 0) 0 else offset + recordCount
    return buildJsonObject {
        put("count", count)
        put("limit", effectiveLimit)
        put("next", pageCount > page)
        put("previous", page > 1)
        put("page", page)
        put("nextPage", page + 1)
        put("previousPage", page - 1)
        put("pageCount", pageCount)
        put("first", page > 1)
        put("last", pageCount > page)
        put("firstRecordCount", firstRecordCount)
        put("lastRecordCount", lastRecordCount)
        put("startPage", startPage)
        put("endPage", endPage)
    }
}
