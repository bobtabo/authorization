package com.authorization.integration

import com.authorization.config.ConfigLoader
import com.authorization.domain.client.ClientStatus
import com.authorization.infrastructure.cache.newRedisPool
import com.authorization.infrastructure.db.initDatabase
import com.authorization.infrastructure.model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.KeyPairGenerator
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID

data class StaffRow(val id: Long, val email: String)
data class ClientRow(val id: Long, val identifier: String, val accessToken: String)
data class InvitationRow(val id: Int, val token: String)
data class NotificationRow(val id: Long, val staffId: Long)

object TestHelper {
    val cfg by lazy { ConfigLoader.load() }

    // 2048-bit RSA 鍵生成は重いため、テスト全体で 1 回だけ生成して再利用する。
    private val cachedKeyPair by lazy {
        val kpg = KeyPairGenerator.getInstance("RSA").also { it.initialize(2048) }
        kpg.generateKeyPair()
    }
    private val cachedPrivateKeyPem: String by lazy {
        val enc = Base64.getMimeEncoder(64, "\n".toByteArray())
        "-----BEGIN PRIVATE KEY-----\n${enc.encodeToString(cachedKeyPair.private.encoded)}\n-----END PRIVATE KEY-----"
    }
    private val cachedPublicKeyPem: String by lazy {
        val enc = Base64.getMimeEncoder(64, "\n".toByteArray())
        "-----BEGIN PUBLIC KEY-----\n${enc.encodeToString(cachedKeyPair.public.encoded)}\n-----END PUBLIC KEY-----"
    }
    val db: Database by lazy {
        val (database, _) = initDatabase(cfg)
        transaction(database) {
            SchemaUtils.create(Staffs, Clients, Invitations, Notifications, JwtHistories)
        }
        database
    }
    private val jedisPool by lazy { newRedisPool(cfg) }

    fun truncateTables() {
        transaction(db) {
            exec("SET FOREIGN_KEY_CHECKS=0")
            listOf("jwt_histories", "notifications", "invitations", "clients", "staffs").forEach { t ->
                exec("TRUNCATE TABLE $t")
            }
            exec("SET FOREIGN_KEY_CHECKS=1")
        }
        jedisPool.resource.use { it.flushDB() }
    }

    fun createStaff(
        name: String = "テストスタッフ",
        email: String = "staff-${UUID.randomUUID().toString().take(8)}@example.com",
        role: Int = 1,
    ): StaffRow {
        val now = LocalDateTime.now()
        val id = transaction(db) {
            Staffs.insertAndGetId {
                it[Staffs.name]       = name
                it[Staffs.email]      = email
                it[Staffs.provider]   = 1
                it[Staffs.providerId] = "test-${UUID.randomUUID().toString().take(8)}"
                it[Staffs.role]       = role
                it[Staffs.createdAt]  = now
                it[Staffs.updatedAt]  = now
                it[Staffs.version]    = 1
            }.value
        }
        return StaffRow(id, email)
    }

    fun createClient(): ClientRow {
        val now = LocalDateTime.now()
        val token      = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "")
        val identifier = "test-client-${UUID.randomUUID().toString().take(8)}"
        val id = transaction(db) {
            Clients.insertAndGetId {
                it[Clients.name]        = "テストクライアント"
                it[Clients.identifier]  = identifier
                it[Clients.postCode]    = "100-0001"
                it[Clients.pref]        = "東京都"
                it[Clients.city]        = "千代田区"
                it[Clients.address]     = "千代田1-1"
                it[Clients.building]    = ""
                it[Clients.tel]         = "0312345678"
                it[Clients.email]       = "client-${UUID.randomUUID().toString().take(8)}@example.com"
                it[Clients.accessToken] = token
                it[Clients.privateKey]  = cachedPrivateKeyPem
                it[Clients.publicKey]   = cachedPublicKeyPem
                it[Clients.fingerprint] = "SHA256:test"
                it[Clients.status]      = ClientStatus.ACTIVE
                it[Clients.createdAt]   = now
                it[Clients.updatedAt]   = now
                it[Clients.createdBy]   = 0
                it[Clients.updatedBy]   = 0
                it[Clients.version]     = 1
            }.value
        }
        return ClientRow(id, identifier, token)
    }

    fun createInvitation(
        token: String = UUID.randomUUID().toString().replace("-", "").take(32),
        role: Int = 2,
    ): InvitationRow {
        val now = LocalDateTime.now()
        val id = transaction(db) {
            Invitations.insertAndGetId {
                it[Invitations.token]     = token
                it[Invitations.role]      = role
                it[Invitations.createdAt] = now
                it[Invitations.updatedAt] = now
            }.value
        }
        return InvitationRow(id, token)
    }

    fun createNotification(staffId: Long, title: String, read: Boolean = false): NotificationRow {
        val now = LocalDateTime.now()
        val id = transaction(db) {
            Notifications.insertAndGetId {
                it[Notifications.staffId]   = staffId
                it[Notifications.title]     = title
                it[Notifications.message]   = "テスト通知本文"
                it[Notifications.read]      = read
                it[Notifications.createdAt] = now
                it[Notifications.createdBy] = 0
                it[Notifications.updatedAt] = now
                it[Notifications.updatedBy] = 0
            }.value
        }
        return NotificationRow(id, staffId)
    }
}
