/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.mappers;

import com.authorization.domain.staff.entities.Staff;
import com.authorization.jooq.tables.records.StaffsRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * スタッフ Entity と jOOQ Record を相互変換する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface StaffRecordMapper {

    /**
     * jOOQレコードをスタッフEntityへ変換します。status は deletedAt の有無から算出します（DBカラムではありません）。
     *
     * @param rec jOOQレコード
     * @return スタッフEntity
     */
    @Mapping(
            target = "provider",
            expression =
                    "java(rec.getProvider() == null ? null :"
                            + " com.authorization.domain.staff.enums.Provider.from(rec.getProvider()))")
    @Mapping(
            target = "role",
            expression =
                    "java(rec.getRole() == null ? null :"
                            + " com.authorization.domain.staff.enums.StaffRole.from(rec.getRole().intValue()))")
    @Mapping(
            target = "status",
            expression =
                    "java(rec.getDeletedAt() == null ?"
                            + " com.authorization.domain.staff.enums.StaffStatus.Active :"
                            + " com.authorization.domain.staff.enums.StaffStatus.Inactive)")
    Staff toEntity(StaffsRecord rec);

    /**
     * スタッフEntityの値をjOOQレコードへ設定します（新規登録用）。
     *
     * @param entity スタッフEntity
     * @param record jOOQレコード（書き込み先）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(
            target = "provider",
            expression = "java(entity.getProvider() == null ? null : entity.getProvider().value())")
    @Mapping(target = "role", expression = "java(entity.getRole() == null ? null : (long) entity.getRole().value())")
    void fillRecord(Staff entity, @MappingTarget StaffsRecord record);
}
