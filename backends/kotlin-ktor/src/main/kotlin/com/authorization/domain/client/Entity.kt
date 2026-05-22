/*
 * クライアントドメインエンティティモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.client

import java.time.LocalDateTime

/**
 * クライアントエンティティです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class Client(
    val id:          Long              = 0,
    val name:        String,
    val identifier:  String            = "",
    val postCode:    String            = "",
    val pref:        String            = "",
    val city:        String            = "",
    val address:     String            = "",
    val building:    String            = "",
    val tel:         String            = "",
    val email:       String            = "",
    val accessToken: String            = "",
    val privateKey:  String            = "",
    val publicKey:   String            = "",
    val fingerprint: String            = "",
    val status:      Int               = ClientStatus.INACTIVE,
    val startAt:     LocalDateTime?    = null,
    val stopAt:      LocalDateTime?    = null,
    val createdAt:   LocalDateTime     = LocalDateTime.now(),
    val createdBy:   Long?             = null,
    val updatedAt:   LocalDateTime     = LocalDateTime.now(),
    val updatedBy:   Long?             = null,
    val deletedAt:   LocalDateTime?    = null,
    val deletedBy:   Long?             = null,
    val version:     Int               = 0,
)

/**
 * JWT履歴エンティティです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
data class JwtHistory(
    val id:        Long           = 0,
    val clientId:  Long,
    val memberId:  String,
    val issueAt:   LocalDateTime,
    val jwt:       String,
    val createdAt: LocalDateTime  = LocalDateTime.now(),
    val deletedAt: LocalDateTime? = null,
)
