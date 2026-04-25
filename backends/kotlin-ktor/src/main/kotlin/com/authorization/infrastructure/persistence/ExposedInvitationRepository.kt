/*
 * 招待リポジトリの Exposed 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.persistence

import com.authorization.config.Config
import com.authorization.domain.invitation.Repository
import com.authorization.domain.invitation.Vo
import org.jetbrains.exposed.sql.Database

/**
 * Exposed を使用した招待リポジトリの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class ExposedInvitationRepository(private val db: Database, private val cfg: Config) : Repository {

    /**
     * 現在有効な招待情報を取得します。
     *
     * @return 招待 VO、または null
     */
    override suspend fun getCurrent(): Vo? = TODO()

    /**
     * 招待トークンを新規発行します。
     *
     * @return 発行された招待 VO
     */
    override suspend fun issue(): Vo = TODO()

    /**
     * 招待トークンに一致する招待情報を取得します。
     *
     * @param token 招待トークン
     * @return 招待 VO、または null
     */
    override suspend fun findByToken(token: String): Vo? = TODO()
}
