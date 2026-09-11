/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.mappers;

import com.authorization.domain.staff.condition.StaffCondition;
import com.authorization.usecases.auth.dtos.SocialDto;
import com.authorization.usecases.staff.dtos.StaffDto;
import org.mapstruct.Mapper;

/**
 * スタッフ DTO を Condition へ変換する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface StaffConditionMapper {

    /**
     * スタッフ DTO を検索条件へ変換します。同名プロパティ（id/keyword/roles/statuses/role）を
     * そのまま引き継ぎます。
     *
     * @param dto スタッフ DTO
     * @return スタッフ検索条件
     */
    StaffCondition toCondition(StaffDto dto);

    /**
     * ソーシャル DTO を検索条件へ変換します（provider/providerId でのログイン検索用）。
     *
     * @param dto ソーシャル DTO
     * @return スタッフ検索条件
     */
    StaffCondition toCondition(SocialDto dto);
}
