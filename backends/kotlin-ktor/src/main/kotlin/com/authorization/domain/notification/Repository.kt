/*
 * 通知ドメインリポジトリインターフェースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.notification

/**
 * 通知リポジトリのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
interface Repository {

    /**
     * 通知をページング取得します。
     *
     * @param staffId スタッフ ID
     * @param cursor ページカーソル
     * @param limit 取得件数
     * @return 通知ページ
     */
    suspend fun listPage(staffId: Long, cursor: String?, limit: Int): Page

    /**
     * 通知の未読件数と総件数を取得します。
     *
     * @param staffId スタッフ ID
     * @return （未読件数, 総件数）のペア
     */
    suspend fun counts(staffId: Long): Pair<Long, Long>

    /**
     * スタッフの通知を一括既読にします。
     *
     * @param staffId スタッフ ID
     * @param ids 対象通知 ID リスト
     * @param all すべて既読にする場合は true
     * @return 更新件数
     */
    suspend fun bulkMarkRead(staffId: Long, ids: List<Long>, all: Boolean): Long

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
    suspend fun store(staffId: Long, messageType: Int, title: String, message: String, createdBy: Long, url: String? = null)

    /**
     * 通知の属性を部分更新します。
     *
     * @param id 通知 ID
     * @param attrs 更新する属性マップ
     * @return 更新成功なら true
     */
    suspend fun patch(id: Long, attrs: Map<String, Any?>): Boolean
}
