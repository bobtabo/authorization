/*
 * 汎用オブジェクトマッピングモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.support

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * ソースオブジェクトから同名プロパティを読み取り、data class T のインスタンスを生成します。
 *
 * @param src コピー元オブジェクト
 * @param convert フィールド名変換マップ（dst名 → src名）
 * @param excludes 除外するパラメータ名のセット（デフォルト値が必要）
 * @return 生成した T のインスタンス
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> create(
    src: Any,
    convert: Map<String, String> = emptyMap(),
    excludes: Set<String> = emptySet(),
): T {
    val ctor = T::class.primaryConstructor
        ?: error("${T::class.simpleName} has no primary constructor")
    val srcProps = src::class.memberProperties.associateBy { it.name }
    val args = ctor.parameters
        .filter { it.name !in excludes }
        .mapNotNull { param ->
            val srcName = convert.getOrDefault(param.name!!, param.name!!)
            val prop = srcProps[srcName] ?: return@mapNotNull null
            param to (prop as KProperty1<Any, *>).get(src)
        }
        .toMap()
    return ctor.callBy(args)
}
