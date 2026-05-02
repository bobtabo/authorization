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
