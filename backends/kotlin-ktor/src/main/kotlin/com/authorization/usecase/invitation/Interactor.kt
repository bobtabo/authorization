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
     * 指定ロールの現在有効な招待情報を取得します。
     *
     * @param role ロール（1=管理者、2=メンバー）
     * @return 招待 VO
     */
    suspend fun current(role: Int): Vo =
        invitationRepo.getCurrentByRole(role) ?: error("invitation_not_found")

    /**
     * 指定ロールで招待トークンを新規発行します。
     *
     * @param role ロール（1=管理者、2=メンバー）
     * @return 発行された招待 VO
     */
    suspend fun issue(role: Int): Vo = invitationRepo.issue(role)

    /**
     * 招待トークンに一致する招待情報を取得し、ロールをキャッシュします。
     *
     * @param dto 検索 DTO
     * @return 招待 VO
     */
    suspend fun findByToken(dto: FindByTokenDto): Vo {
        val vo = invitationRepo.findByToken(dto.token) ?: error("invitation_not_found")
        invitationAuthRepo.store(vo.token, vo.role, 600L)
        return vo
    }
}
