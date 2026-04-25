/*
 * Exposed ORM テーブル定義モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.model

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/** クライアントテーブル定義です。 */
object Clients : LongIdTable("clients") {
    val name        = varchar("name", 255)
    val identifier  = varchar("identifier", 255).uniqueIndex()
    val postCode    = varchar("post_code", 255).default("")
    val pref        = varchar("pref", 255).default("")
    val city        = varchar("city", 255).default("")
    val address     = varchar("address", 255).default("")
    val building    = varchar("building", 255).default("")
    val tel         = varchar("tel", 255).default("")
    val email       = varchar("email", 255).default("")
    val accessToken = varchar("access_token", 512).uniqueIndex()
    val privateKey  = text("private_key")
    val publicKey   = text("public_key")
    val fingerprint = varchar("fingerprint", 255)
    val status      = integer("status").default(1)
    val startAt     = datetime("start_at").nullable()
    val stopAt      = datetime("stop_at").nullable()
    val createdAt   = datetime("created_at")
    val createdBy   = integer("created_by").nullable()
    val updatedAt   = datetime("updated_at")
    val updatedBy   = integer("updated_by").nullable()
    val deletedAt   = datetime("deleted_at").nullable()
    val deletedBy   = integer("deleted_by").nullable()
    val version     = integer("version").default(0)
}

/** スタッフテーブル定義です。 */
object Staffs : LongIdTable("staffs") {
    val name        = varchar("name", 255)
    val email       = varchar("email", 255).uniqueIndex()
    val provider    = integer("provider")
    val providerId  = varchar("provider_id", 255)
    val avatar      = varchar("avatar", 255).nullable()
    val role        = integer("role").default(2)
    val lastLoginAt = datetime("last_login_at").nullable()
    val createdAt   = datetime("created_at")
    val createdBy   = integer("created_by").nullable()
    val updatedAt   = datetime("updated_at")
    val updatedBy   = integer("updated_by").nullable()
    val deletedAt   = datetime("deleted_at").nullable()
    val deletedBy   = integer("deleted_by").nullable()
    val version     = integer("version").default(0)
}

/** 招待テーブル定義です。 */
object Invitations : IntIdTable("invitations") {
    val token     = varchar("token", 255).uniqueIndex()
    val createdAt = datetime("created_at")
    val createdBy = integer("created_by").nullable()
    val updatedAt = datetime("updated_at")
    val updatedBy = integer("updated_by").nullable()
    val deletedAt = datetime("deleted_at").nullable()
    val deletedBy = integer("deleted_by").nullable()
    val version   = integer("version").default(0)
}

/** 通知テーブル定義です。 */
object Notifications : LongIdTable("notifications") {
    val staffId     = long("staff_id")
    val messageType = integer("message_type").default(1)
    val title       = varchar("title", 255)
    val message     = varchar("message", 512).default("")
    val url         = varchar("url", 255).nullable()
    val read        = bool("read").default(false)
    val createdAt   = datetime("created_at")
    val createdBy   = integer("created_by").default(0)
    val updatedAt   = datetime("updated_at")
    val updatedBy   = integer("updated_by").default(0)
    val deletedAt   = datetime("deleted_at").nullable()
    val deletedBy   = integer("deleted_by").nullable()
    val version     = integer("version").default(1)
}
