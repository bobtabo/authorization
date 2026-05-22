/*
 * ゲートユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.gate

import com.authorization.config.Config
import com.authorization.domain.client.JwtHistoryRepository
import com.authorization.domain.client.Repository as ClientRepository
import com.authorization.domain.gate.CacheRepository
import com.authorization.domain.gate.IssueVo
import com.authorization.domain.gate.VerifyVo
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.time.LocalDateTime
import java.util.Base64
import java.util.Date
import java.util.UUID

/**
 * ゲート認可ユースケースの Interactor です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Interactor(
    private val clientRepo: ClientRepository,
    private val cache: CacheRepository,
    private val cfg: Config,
    private val historyRepo: JwtHistoryRepository? = null,
) {

    /**
     * クライアントのアクセストークンを検証して JWT を発行します。
     *
     * @param dto 発行 DTO
     * @return 発行結果 VO
     */
    suspend fun issueToken(dto: IssueDto): IssueVo {
        val c = clientRepo.findByAccessToken(dto.accessToken)
            ?: error("client_not_found")

        val cached = cache.getJwt(c.identifier, dto.memberId)
        if (!cached.isNullOrBlank()) return IssueVo(token = cached)

        val token = issueJwt(
            memberId      = dto.memberId,
            identifier    = c.identifier,
            privateKeyPem = c.privateKey,
            fingerprint   = c.fingerprint,
            issuer        = cfg.jwt.issuer,
            ttl           = cfg.jwt.ttl,
        )
        runCatching { cache.putJwt(c.identifier, dto.memberId, token, cfg.jwt.cacheTtl) }
        historyRepo?.let { runCatching { it.save(c.id, dto.memberId, LocalDateTime.now(), token) } }
        return IssueVo(token = token)
    }

    /**
     * JWT トークンを検証してクレームを返します。
     *
     * @param dto 検証 DTO
     * @return 検証結果 VO
     */
    suspend fun verify(dto: VerifyDto): VerifyVo {
        val c = clientRepo.findByIdentifier(dto.identifier)
            ?: error("client_not_found")
        val claims = verifyJwt(dto.identifier, dto.token, c.publicKey, cfg.jwt.issuer)
        return VerifyVo(claims = claims)
    }
}

private fun issueJwt(
    memberId: String,
    identifier: String,
    privateKeyPem: String,
    fingerprint: String,
    issuer: String,
    ttl: Long,
): String {
    val privateKey = loadPrivateKey(privateKeyPem)
    val now        = Instant.now()
    val claimsSet  = JWTClaimsSet.Builder()
        .issuer(issuer)
        .subject(memberId)
        .audience(identifier)
        .expirationTime(Date.from(now.plusSeconds(ttl)))
        .issueTime(Date.from(now))
        .notBeforeTime(Date.from(now))
        .jwtID(UUID.randomUUID().toString())
        .build()
    val header = JWSHeader.Builder(JWSAlgorithm.RS256).keyID(fingerprint).build()
    val jwt    = SignedJWT(header, claimsSet)
    jwt.sign(RSASSASigner(privateKey))
    return jwt.serialize()
}

private fun verifyJwt(
    identifier: String,
    tokenStr: String,
    publicKeyPem: String,
    issuer: String,
): Map<String, Any?> {
    val publicKey = loadPublicKey(publicKeyPem)
    val jwt       = SignedJWT.parse(tokenStr)
    check(jwt.verify(RSASSAVerifier(publicKey))) { "invalid_token" }
    val claims = jwt.jwtClaimsSet
    check(claims.issuer == issuer) { "invalid_issuer" }
    check(claims.audience.contains(identifier)) { "invalid_audience" }
    check(Date().before(claims.expirationTime)) { "token_expired" }
    return claims.toJSONObject().mapValues { it.value }
}

private fun loadPrivateKey(pem: String): RSAPrivateKey {
    val cleaned = pem
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace(Regex("\\s"), "")
    val bytes = Base64.getDecoder().decode(cleaned)
    return KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(bytes)) as RSAPrivateKey
}

private fun loadPublicKey(pem: String): RSAPublicKey {
    val cleaned = pem
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replace(Regex("\\s"), "")
    val bytes = Base64.getDecoder().decode(cleaned)
    return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(bytes)) as RSAPublicKey
}
