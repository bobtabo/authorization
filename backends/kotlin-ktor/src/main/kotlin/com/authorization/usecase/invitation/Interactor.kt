/*
 * 招待ユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.invitation

import com.authorization.domain.invitation.AuthRepository
import com.authorization.domain.invitation.Repository
import com.authorization.domain.invitation.Vo

/**
 * 招待ユースケースの Interactor です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Interactor(
    private val invitationRepo: Repository,
    private val invitationAuthRepo: AuthRepository,
) {

    /**
     * 現在有効な招待情報を取得します。
     *
     * @return 招待 VO
     */
    suspend fun current(): Vo =
        invitationRepo.getCurrent() ?: error("invitation_not_found")

    /**
     * 招待トークンを新規発行します。
     *
     * @return 発行された招待 VO
     */
    suspend fun issue(): Vo = invitationRepo.issue()

    /**
     * 招待トークンに一致する招待情報を取得し、認証トークンをキャッシュします。
     *
     * @param dto 検索 DTO
     * @return 招待 VO
     */
    suspend fun findByToken(dto: FindByTokenDto): Vo {
        val vo = invitationRepo.findByToken(dto.token) ?: error("invitation_not_found")
        invitationAuthRepo.store(vo.token, 600L)
        return vo
    }
}
