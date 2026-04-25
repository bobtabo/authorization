/*
 * 認証ユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.auth

import com.authorization.domain.staff.Repository
import com.authorization.domain.staff.Staff

/**
 * 認証ユースケースの Interactor です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Interactor(private val staffRepo: Repository) {

    /**
     * 指定した ID のスタッフを取得します。
     *
     * @param id スタッフ ID
     * @return スタッフエンティティ
     */
    suspend fun findUser(id: Long): Staff = TODO()

    /**
     * ログインを処理してスタッフを返します。
     *
     * @param dto ログイン DTO
     * @return スタッフエンティティ
     */
    suspend fun login(dto: LoginDto): Staff = TODO()
}
