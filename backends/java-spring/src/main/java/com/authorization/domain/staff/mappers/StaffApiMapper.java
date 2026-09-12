/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.mappers;

import com.authorization.domain.staff.entities.Staff;
import com.authorization.domain.staff.enums.StaffStatus;
import com.authorization.domain.staff.valueobjects.StaffResourceVo;
import com.authorization.domain.staff.valueobjects.StaffVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * スタッフ Entity を API 向け ValueObject へ変換する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring", imports = StaffStatus.class)
public interface StaffApiMapper {

    /**
     * スタッフ Entity を一覧・詳細向け ValueObject に変換します。
     * status は role と異なり Entity の値をそのまま使わず、deletedAt の有無から再計算します
     * （PHP 版 {@code StaffApiMapper::toListItem} と同じ挙動）。
     *
     * @param staff スタッフ Entity
     * @return スタッフリソース ValueObject
     */
    @Mapping(target = "found", constant = "true")
    @Mapping(target = "role", expression = "java(staff.getRole() == null ? null : staff.getRole().value())")
    @Mapping(target = "status", expression =
            "java(staff.getDeletedAt() == null ? StaffStatus.Active.value() : StaffStatus.Inactive.value())")
    StaffResourceVo toResourceVo(Staff staff);

    /**
     * スタッフ Entity を認証用 ValueObject に変換します。status は Entity に既に設定済みの値
     * （リポジトリ読み込み時に deletedAt から算出済み）をそのまま使います。
     *
     * @param staff スタッフ Entity
     * @return スタッフ ValueObject
     */
    @Mapping(target = "role", expression = "java(staff.getRole() == null ? null : staff.getRole().value())")
    @Mapping(target = "status", expression = "java(staff.getStatus() == null ? null : staff.getStatus().value())")
    StaffVo toVo(Staff staff);
}
