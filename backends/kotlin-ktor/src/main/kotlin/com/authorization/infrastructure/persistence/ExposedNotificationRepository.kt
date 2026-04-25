/*
 * 通知リポジトリの Exposed 実装モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.infrastructure.persistence

import com.authorization.domain.notification.Notification
import com.authorization.domain.notification.Page
import com.authorization.domain.notification.Repository
import org.jetbrains.exposed.sql.Database

/**
 * Exposed を使用した通知リポジトリの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class ExposedNotificationRepository(private val db: Database) : Repository {

    /**
     * 通知をページング取得します。
     *
     * @param staffId スタッフ ID
     * @param cursor ページカーソル
     * @param limit 取得件数
     * @return 通知ページ
     */
    override suspend fun listPage(staffId: Long, cursor: String?, limit: Int): Page = TODO()

    /**
     * 通知の未読件数と総件数を取得します。
     *
     * @param staffId スタッフ ID
     * @return （未読件数, 総件数）のペア
     */
    override suspend fun counts(staffId: Long): Pair<Long, Long> = TODO()

    /**
     * スタッフの通知を一括既読にします。
     *
     * @param staffId スタッフ ID
     * @param ids 対象通知 ID リスト
     * @param all すべて既読にする場合は true
     * @return 更新件数
     */
    override suspend fun bulkMarkRead(staffId: Long, ids: List<Long>, all: Boolean): Long = TODO()

    /**
     * 通知を保存します。
     *
     * @param staffId 宛先スタッフ ID
     * @param messageType メッセージ種別
     * @param title タイトル
     * @param message 本文
     * @param createdBy 作成者スタッフ ID
     * @param url 関連 URL
     */
    override suspend fun store(staffId: Long, messageType: Int, title: String, message: String, createdBy: Long, url: String?) = TODO()

    /**
     * 通知の属性を部分更新します。
     *
     * @param id 通知 ID
     * @param attrs 更新する属性マップ
     * @return 更新成功なら true
     */
    override suspend fun patch(id: Long, attrs: Map<String, Any?>): Boolean = TODO()
}
