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
        if (dto.executorId == 0L) {
            throw AppException(401, "unauthenticated")
        }
        if (dto.role != StaffRole.ADMIN && dto.role != StaffRole.MEMBER) {
            throw AppException(400, "role_invalid")
        }
        val executor = repo.findById(dto.executorId)
        if (executor == null || executor.deletedAt != null || executor.role != StaffRole.ADMIN) {
            throw AppException(403, "forbidden")
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
