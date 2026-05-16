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
import org.jetbrains.exposed.sql.and
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
     * 指定ロールの現在有効な招待情報を取得します。
     *
     * @param role ロール（1=管理者、2=メンバー）
     * @return 招待 VO、または null
     */
    override suspend fun getCurrentByRole(role: Int): Vo? = newSuspendedTransaction(db = db) {
        Invitations.selectAll()
            .where { (Invitations.role eq role) and Invitations.deletedAt.isNull() }
            .orderBy(Invitations.createdAt to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.let { buildVo(it[Invitations.token], it[Invitations.role]) }
    }

    /**
     * 指定ロールで招待トークンを新規発行します。
     *
     * @param role ロール（1=管理者、2=メンバー）
     * @return 発行された招待 VO
     */
    override suspend fun issue(role: Int): Vo = newSuspendedTransaction(db = db) {
        val now   = LocalDateTime.now()
        val token = generateHex(16)
        Invitations.insert {
            it[Invitations.token]     = token
            it[Invitations.role]      = role
            it[Invitations.createdAt] = now
            it[Invitations.createdBy] = 0
            it[Invitations.updatedAt] = now
            it[Invitations.updatedBy] = 0
        }
        buildVo(token, role)
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
            ?.let { buildVo(it[Invitations.token], it[Invitations.role]) }
    }

    private fun generateHex(byteCount: Int): String {
        val buf = ByteArray(byteCount)
        SecureRandom().nextBytes(buf)
        return buf.joinToString("") { "%02x".format(it) }
    }

    private fun buildVo(token: String, role: Int): Vo {
        val url        = "${cfg.app.frontendUrl}/invitation/$token"
        val displayUrl = buildDisplayUrl(url)
        return Vo(token = token, role = role, url = url, displayUrl = displayUrl)
    }

    private fun buildDisplayUrl(url: String): String {
        val seg = "/invitation/"
        val idx = url.indexOf(seg)
        if (idx != -1) {
            val base   = url.substring(0, idx + seg.length)
            val after  = url.substring(idx + seg.length)
            val tokEnd = after.indexOfFirst { it == '?' || it == '#' }.takeIf { it >= 0 } ?: after.length
            val tok    = after.substring(0, tokEnd)
            val suffix = after.substring(tokEnd)
            if (tok.length > 13) return "${base}${tok.take(6)}...${tok.takeLast(4)}$suffix"
        }
        return if (url.length > 72) url.take(68) + "..." else url
    }
}
