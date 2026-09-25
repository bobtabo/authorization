/*
 * スタッフユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.staff

import com.authorization.domain.staff.Condition
import com.authorization.domain.staff.ListItem
import com.authorization.domain.staff.Repository
import com.authorization.domain.staff.StaffRole
import com.authorization.support.AppException

/**
 * スタッフユースケースの Interactor です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Interactor(private val repo: Repository) {

    /**
     * 検索条件に一致するスタッフ一覧と総件数を取得します。
     *
     * @param cond 検索条件
     * @return スタッフ一覧と総件数のペア
     */
    suspend fun findByConditionWithCount(cond: Condition): Pair<List<ListItem>, Int> {
        val count = repo.countByCondition(cond)
        val items = repo.findByCondition(cond).map { s ->
            ListItem(
                id        = s.id,
                name      = s.name,
                email     = s.email,
                role      = s.role,
                status    = if (s.deletedAt != null) 0 else 1,
                version   = s.version,
                createdAt = s.createdAt,
                updatedAt = s.updatedAt,
            )
        }
        return Pair(items, count)
    }

    /**
     * スタッフのロールを更新します。
     *
     * @param dto ロール更新 DTO
     */
    suspend fun updateRole(dto: UpdateRoleDto) {
        authorizeAdminExecutor(dto.executorId)
        if (dto.role != StaffRole.ADMIN && dto.role != StaffRole.MEMBER) {
            throw AppException(400, "role_invalid")
        }
        // 無効化（論理削除）はログイン可否にのみ影響するため、権限更新は無効スタッフも対象に含める。
        repo.findById(dto.id) ?: throw AppException(404, "staff_not_found")
        val ok = repo.updateRole(dto.id, dto.role, dto.executorId)
        if (!ok) {
            throw AppException(404, "staff_not_found")
        }
    }

    /**
     * 論理削除されたスタッフを復元します。
     *
     * @param dto 復元 DTO
     */
    suspend fun restore(dto: RestoreDto) {
        authorizeAdminExecutor(dto.executorId)
        repo.restore(dto.id)
    }

    /**
     * スタッフを論理削除します。
     *
     * @param dto 削除 DTO
     */
    suspend fun destroy(dto: DestroyDto) {
        authorizeAdminExecutor(dto.executorId)
        val staff = repo.findById(dto.id) ?: throw AppException(404, "staff_not_found")
        repo.softDelete(dto.id, dto.executorId, staff.version)
    }

    /**
     * 実行者が有効なAdminであることを検証します。
     * 未認証の場合は401、Admin以外または無効化済みの場合は403を発生させます。
     *
     * @param executorId 実行者スタッフ ID
     */
    private suspend fun authorizeAdminExecutor(executorId: Long) {
        if (executorId == 0L) {
            throw AppException(401, "unauthenticated")
        }
        val executor = repo.findById(executorId)
        if (executor == null || executor.deletedAt != null || executor.role != StaffRole.ADMIN) {
            throw AppException(403, "forbidden")
        }
    }
}
