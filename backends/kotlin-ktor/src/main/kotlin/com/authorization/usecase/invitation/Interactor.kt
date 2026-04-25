/*
 * 招待ユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.invitation

import com.authorization.domain.invitation.Repository
import com.authorization.domain.invitation.Vo

/**
 * 招待ユースケースの Interactor です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Interactor(private val repo: Repository) {

    /**
     * 現在有効な招待情報を取得します。
     *
     * @return 招待 VO
     */
    suspend fun current(): Vo = TODO()

    /**
     * 招待トークンを新規発行します。
     *
     * @return 発行された招待 VO
     */
    suspend fun issue(): Vo = TODO()

    /**
     * 招待トークンに一致する招待情報を取得します。
     *
     * @param dto 検索 DTO
     * @return 招待 VO
     */
    suspend fun findByToken(dto: FindByTokenDto): Vo = TODO()
}
