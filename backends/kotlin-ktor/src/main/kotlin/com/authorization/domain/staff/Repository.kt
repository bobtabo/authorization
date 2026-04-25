/*
 * スタッフドメインリポジトリインターフェースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.domain.staff

/**
 * スタッフリポジトリのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
interface Repository {

    /**
     * 検索条件に一致するスタッフ一覧を取得します。
     *
     * @param cond 検索条件
     * @return スタッフ一覧
     */
    suspend fun findByCondition(cond: Condition): List<Staff>

    /**
     * 指定した ID のスタッフを取得します。
     *
     * @param id スタッフ ID
     * @return スタッフ、または null
     */
    suspend fun findById(id: Long): Staff?

    /**
     * プロバイダー情報に一致するスタッフを取得します。
     *
     * @param provider プロバイダー種別
     * @param providerId プロバイダー ID
     * @return スタッフ、または null
     */
    suspend fun findByProvider(provider: Int, providerId: String): Staff?

    /**
     * アクティブなスタッフ全員を取得します。
     *
     * @return スタッフ一覧
     */
    suspend fun findAllActive(): List<Staff>

    /**
     * スタッフを保存します（新規登録・更新）。
     *
     * @param s スタッフエンティティ
     * @return 保存後のスタッフエンティティ
     */
    suspend fun save(s: Staff): Staff

    /**
     * スタッフのロールを更新します。
     *
     * @param id スタッフ ID
     * @param role 新しいロール
     * @param updatedBy 更新者スタッフ ID
     * @return 更新成功なら true
     */
    suspend fun updateRole(id: Long, role: Int, updatedBy: Long): Boolean

    /**
     * スタッフを論理削除します。
     *
     * @param id スタッフ ID
     * @param deletedBy 削除者スタッフ ID
     * @return 削除成功なら true
     */
    suspend fun softDelete(id: Long, deletedBy: Long): Boolean

    /**
     * 論理削除されたスタッフを復元します。
     *
     * @param id スタッフ ID
     * @return 復元成功なら true
     */
    suspend fun restore(id: Long): Boolean
}
