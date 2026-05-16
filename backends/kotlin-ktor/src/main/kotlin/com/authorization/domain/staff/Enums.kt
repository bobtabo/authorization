/*
 * スタッフドメイン列挙値モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.staff

/**
 * スタッフロールの定数です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
object StaffRole {
    /** 管理者 */
    const val ADMIN:  Int = 1
    /** 一般メンバー */
    const val MEMBER: Int = 2

    /**
     * 整数値からロール定数を返します。
     * 未知の値の場合は MEMBER を返します。
     *
     * @param value ロール整数値
     * @return ADMIN または MEMBER
     */
    fun from(value: Int): Int = if (value == ADMIN) ADMIN else MEMBER
}

/**
 * スタッフ認証プロバイダーの定数です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
object StaffProvider {
    /** Google OAuth */
    const val GOOGLE: Int = 1
    /** GitHub OAuth */
    const val GITHUB: Int = 2
}
