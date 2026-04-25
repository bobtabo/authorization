/*
 * クライアントユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.client

import com.authorization.domain.client.Client
import com.authorization.domain.client.DetailVo
import com.authorization.domain.client.ListItem
import com.authorization.domain.client.Repository
import com.authorization.domain.client.StoreResultVo

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
    suspend fun findByCondition(dto: ListConditionDto): List<Client> = TODO()

    /**
     * 指定した ID のクライアントを取得します。
     *
     * @param id クライアント ID
     * @return クライアント詳細
     */
    suspend fun findById(id: Long): Client = TODO()

    /**
     * クライアントを新規登録します。
     *
     * @param dto 登録 DTO
     * @return 登録結果 VO
     */
    suspend fun store(dto: StoreDto): StoreResultVo = TODO()

    /**
     * クライアントを更新します。
     *
     * @param dto 更新 DTO
     * @return 更新後のクライアント詳細 VO
     */
    suspend fun update(dto: UpdateDto): DetailVo = TODO()

    /**
     * クライアントを論理削除します。
     *
     * @param id クライアント ID
     * @param executorId 実行者スタッフ ID
     */
    suspend fun destroy(id: Long, executorId: Long): Unit = TODO()

    /**
     * アクセストークンに一致するクライアントを取得します。
     *
     * @param token アクセストークン
     * @return クライアント、または null
     */
    suspend fun findByAccessToken(token: String): Client? = TODO()

    /**
     * 識別子に一致するクライアントを取得します。
     *
     * @param identifier 識別子
     * @return クライアント、または null
     */
    suspend fun findByIdentifier(identifier: String): Client? = TODO()
}
