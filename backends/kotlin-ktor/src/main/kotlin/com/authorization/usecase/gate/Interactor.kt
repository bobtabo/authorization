/*
 * ゲートユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.gate

import com.authorization.config.Config
import com.authorization.domain.client.Repository as ClientRepository
import com.authorization.domain.gate.CacheRepository
import com.authorization.domain.gate.IssueVo
import com.authorization.domain.gate.VerifyVo

/**
 * ゲート認可ユースケースの Interactor です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Interactor(
    private val clientRepo: ClientRepository,
    private val cache: CacheRepository,
    private val cfg: Config,
) {

    /**
     * クライアントのアクセストークンを検証して JWT を発行します。
     *
     * @param dto 発行 DTO
     * @return 発行結果 VO
     */
    suspend fun issueToken(dto: IssueDto): IssueVo = TODO()

    /**
     * JWT トークンを検証してクレームを返します。
     *
     * @param dto 検証 DTO
     * @return 検証結果 VO
     */
    suspend fun verify(dto: VerifyDto): VerifyVo = TODO()
}
