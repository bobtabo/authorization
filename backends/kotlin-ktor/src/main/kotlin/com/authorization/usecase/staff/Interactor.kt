/*
 * スタッフユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.staff

import com.authorization.domain.staff.Condition
import com.authorization.domain.staff.ListItem
import com.authorization.domain.staff.Repository
import com.authorization.support.AppException

/**
 * スタッフユースケースの Interactor です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Interactor(private val repo: Repository) {

    /**
     * 検索条件に一致するスタッフ一覧を取得します。
     *
     * @param cond 検索条件
     * @return スタッフ一覧
     */
    suspend fun findByCondition(cond: Condition): List<ListItem> =
        repo.findByCondition(cond).map { s ->
            ListItem(
                id        = s.id,
                name      = s.name,
                email     = s.email,
                role      = s.role,
                status    = if (s.deletedAt != null) 0 else 1,
                createdAt = s.createdAt,
                updatedAt = s.updatedAt,
            )
        }

    /**
     * スタッフのロールを更新します。
     *
     * @param dto ロール更新 DTO
     */
    suspend fun updateRole(dto: UpdateRoleDto) {
        repo.updateRole(dto.id, dto.role, dto.executorId)
    }

    /**
     * 論理削除されたスタッフを復元します。
     *
     * @param id スタッフ ID
     */
    suspend fun restore(id: Long) {
        repo.restore(id)
    }

    /**
     * スタッフを論理削除します。
     *
     * @param dto 削除 DTO
     */
    suspend fun destroy(dto: DestroyDto) {
        val staff = repo.findById(dto.id) ?: throw AppException(404, "staff_not_found")
        repo.softDelete(dto.id, dto.executorId, staff.version)
    }
}
