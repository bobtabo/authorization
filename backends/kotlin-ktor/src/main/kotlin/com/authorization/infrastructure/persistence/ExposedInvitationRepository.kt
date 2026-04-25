/*
 * 招待リポジトリの Exposed 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.persistence

import com.authorization.config.Config
import com.authorization.domain.invitation.Repository
import com.authorization.domain.invitation.Vo
import com.authorization.infrastructure.model.Invitations
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.security.SecureRandom
import java.time.LocalDateTime

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
    override suspend fun getCurrent(): Vo? = newSuspendedTransaction(db = db) {
        Invitations.selectAll()
            .where { Invitations.deletedAt.isNull() }
            .orderBy(Invitations.createdAt to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.let { buildVo(it[Invitations.token]) }
    }

    /**
     * 招待トークンを新規発行します。
     *
     * @return 発行された招待 VO
     */
    override suspend fun issue(): Vo = newSuspendedTransaction(db = db) {
        val now   = LocalDateTime.now()
        val token = generateHex(16)
        Invitations.insert {
            it[Invitations.token]     = token
            it[Invitations.createdAt] = now
            it[Invitations.updatedAt] = now
        }
        buildVo(token)
    }

    /**
     * 招待トークンに一致する招待情報を取得します。
     *
     * @param token 招待トークン
     * @return 招待 VO、または null
     */
    override suspend fun findByToken(token: String): Vo? = newSuspendedTransaction(db = db) {
        Invitations.selectAll()
            .where { (Invitations.token eq token) and Invitations.deletedAt.isNull() }
            .firstOrNull()
            ?.let { buildVo(it[Invitations.token]) }
    }

    private fun generateHex(byteCount: Int): String {
        val buf = ByteArray(byteCount)
        SecureRandom().nextBytes(buf)
        return buf.joinToString("") { "%02x".format(it) }
    }

    private fun buildVo(token: String): Vo {
        val url        = "${cfg.app.frontendUrl}/register?token=$token"
        val displayUrl = if (url.length > 50) url.substring(0, 47) + "..." else url
        return Vo(token = token, url = url, displayUrl = displayUrl)
    }
}
