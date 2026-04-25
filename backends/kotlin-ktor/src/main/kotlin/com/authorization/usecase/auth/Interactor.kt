/*
 * 認証ユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
package com.authorization.usecase.auth

import com.authorization.domain.staff.Repository
import com.authorization.domain.staff.Staff
import com.authorization.domain.staff.StaffRole
import java.time.LocalDateTime

/**
 * 認証ユースケースの Interactor です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class Interactor(private val staffRepo: Repository) {

    /**
     * 指定した ID のスタッフを取得します。
     *
     * @param id スタッフ ID
     * @return スタッフエンティティ
     */
    suspend fun findUser(id: Long): Staff =
        staffRepo.findById(id) ?: error("staff_not_found")

    /**
     * ログインを処理してスタッフを返します。
     *
     * @param dto ログイン DTO
     * @return スタッフエンティティ
     */
    suspend fun login(dto: LoginDto): Staff {
        val now = LocalDateTime.now()
        val existing = staffRepo.findByProvider(dto.provider, dto.providerId)
        val staff = existing?.copy(
            avatar      = dto.avatar,
            lastLoginAt = now,
            updatedAt   = now,
        ) ?: Staff(
            name        = dto.name,
            email       = dto.email,
            provider    = dto.provider,
            providerId  = dto.providerId,
            avatar      = dto.avatar,
            role        = StaffRole.MEMBER,
            lastLoginAt = now,
            createdAt   = now,
            updatedAt   = now,
        )
        return staffRepo.save(staff)
    }
}
