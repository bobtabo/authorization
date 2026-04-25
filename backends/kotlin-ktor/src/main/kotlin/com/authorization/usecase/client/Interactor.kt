/*
 * クライアントユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.client

import com.authorization.domain.client.Client
import com.authorization.domain.client.ClientStatus
import com.authorization.domain.client.Condition
import com.authorization.domain.client.DetailVo
import com.authorization.domain.client.Repository
import com.authorization.domain.client.StoreResultVo
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64

/**
 * クライアントユースケースの Interactor です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Interactor(private val repo: Repository) {

    /**
     * 検索条件に一致するクライアント一覧を取得します。
     *
     * @param dto 検索条件 DTO
     * @return クライアント一覧
     */
    suspend fun findByCondition(dto: ListConditionDto): List<Client> {
        val cond = Condition(
            keyword   = dto.keyword,
            startFrom = dto.startFrom?.let { parseDateTime(it) },
            startTo   = dto.startTo?.let { parseDateTime(it) },
            statuses  = dto.statuses,
        )
        return repo.findByCondition(cond)
    }

    /**
     * 指定した ID のクライアントを取得します。
     *
     * @param id クライアント ID
     * @return クライアント詳細
     */
    suspend fun findById(id: Long): Client =
        repo.findById(id) ?: error("client_not_found")

    /**
     * クライアントを新規登録します。
     *
     * @param dto 登録 DTO
     * @return 登録結果 VO
     */
    suspend fun store(dto: StoreDto): StoreResultVo {
        val (privPem, pubPem, fingerprint) = generateRsaKeys()
        val accessToken = generateHex(32)
        val identifier  = generateHex(8)
        val now = LocalDateTime.now()
        val c = Client(
            name        = dto.name,
            identifier  = identifier,
            postCode    = dto.postCode,
            pref        = dto.pref,
            city        = dto.city,
            address     = dto.address,
            building    = dto.building,
            tel         = dto.tel,
            email       = dto.email,
            accessToken = accessToken,
            privateKey  = privPem,
            publicKey   = pubPem,
            fingerprint = fingerprint,
            status      = ClientStatus.INACTIVE,
            createdAt   = now,
            createdBy   = dto.executorId,
            updatedAt   = now,
            updatedBy   = dto.executorId,
        )
        val saved = repo.save(c)
        return StoreResultVo(
            id          = saved.id,
            name        = saved.name,
            email       = saved.email,
            accessToken = saved.accessToken,
        )
    }

    /**
     * クライアントを更新します。
     *
     * @param dto 更新 DTO
     * @return 更新後のクライアント詳細 VO
     */
    suspend fun update(dto: UpdateDto): DetailVo {
        var c = repo.findById(dto.id) ?: error("client_not_found")
        dto.name?.let     { v -> c = c.copy(name     = v) }
        dto.postCode?.let { v -> c = c.copy(postCode = v) }
        dto.pref?.let     { v -> c = c.copy(pref     = v) }
        dto.city?.let     { v -> c = c.copy(city     = v) }
        dto.address?.let  { v -> c = c.copy(address  = v) }
        dto.building?.let { v -> c = c.copy(building = v) }
        dto.tel?.let      { v -> c = c.copy(tel      = v) }
        dto.email?.let    { v -> c = c.copy(email    = v) }
        dto.status?.let { status ->
            val now = LocalDateTime.now()
            c = when (status) {
                ClientStatus.ACTIVE    -> c.copy(status = status, startAt = c.startAt ?: now, stopAt = null)
                ClientStatus.SUSPENDED -> c.copy(status = status, stopAt = now)
                else                   -> c.copy(status = status)
            }
        }
        c = c.copy(updatedAt = LocalDateTime.now(), updatedBy = dto.executorId)
        val saved = repo.save(c)
        return DetailVo(
            id         = saved.id,
            name       = saved.name,
            identifier = saved.identifier,
            postCode   = saved.postCode,
            pref       = saved.pref,
            city       = saved.city,
            address    = saved.address,
            building   = saved.building,
            tel        = saved.tel,
            email      = saved.email,
            status     = saved.status,
            startAt    = saved.startAt,
            stopAt     = saved.stopAt,
            createdAt  = saved.createdAt,
            updatedAt  = saved.updatedAt,
        )
    }

    /**
     * クライアントを論理削除します。
     *
     * @param id クライアント ID
     * @param executorId 実行者スタッフ ID
     */
    suspend fun destroy(id: Long, executorId: Long) {
        var c = repo.findById(id) ?: error("client_not_found")
        val now = LocalDateTime.now()
        c = c.copy(status = ClientStatus.CLOSED, updatedAt = now, updatedBy = executorId)
        repo.save(c)
        repo.softDelete(id, executorId)
    }

    /**
     * アクセストークンに一致するクライアントを取得します。
     *
     * @param token アクセストークン
     * @return クライアント、または null
     */
    suspend fun findByAccessToken(token: String): Client? = repo.findByAccessToken(token)

    /**
     * 識別子に一致するクライアントを取得します。
     *
     * @param identifier 識別子
     * @return クライアント、または null
     */
    suspend fun findByIdentifier(identifier: String): Client? = repo.findByIdentifier(identifier)
}

private fun parseDateTime(s: String): LocalDateTime? {
    val fmtFull = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val fmtDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return runCatching { LocalDateTime.parse(s, fmtFull) }.getOrElse {
        runCatching { LocalDate.parse(s, fmtDate).atStartOfDay() }.getOrNull()
    }
}

private fun generateHex(byteCount: Int): String {
    val buf = ByteArray(byteCount)
    SecureRandom().nextBytes(buf)
    return buf.joinToString("") { "%02x".format(it) }
}

private fun generateRsaKeys(): Triple<String, String, String> {
    val kpg = KeyPairGenerator.getInstance("RSA")
    kpg.initialize(4096)
    val kp = kpg.generateKeyPair()

    val enc    = Base64.getMimeEncoder(64, "\n".toByteArray())
    val privPem = "-----BEGIN PRIVATE KEY-----\n${enc.encodeToString(kp.private.encoded)}\n-----END PRIVATE KEY-----\n"
    val pubPem  = "-----BEGIN PUBLIC KEY-----\n${enc.encodeToString(kp.public.encoded)}\n-----END PUBLIC KEY-----\n"

    val hash        = MessageDigest.getInstance("SHA-256").digest(kp.public.encoded)
    val fingerprint = "SHA256:" + Base64.getEncoder().withoutPadding().encodeToString(hash)

    return Triple(privPem, pubPem, fingerprint)
}
