/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.invitation.mappers;

import com.authorization.domain.invitation.entities.Invitation;
import com.authorization.jooq.tables.records.InvitationsRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 招待 Entity と jOOQ Record を相互変換する MapStruct マッパーです。招待は事前投入済みの行を
 * 更新するだけで新規作成しないため、fillRecord は更新用途のみです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface InvitationRecordMapper {

    /**
     * jOOQレコードを招待Entityへ変換します。createdBy/updatedBy/deletedAt/deletedBy も
     * マッピングします（issue()がこのEntityを再度fillRecordで書き戻すため、無視すると
     * created_by等がnullになってUPDATEが失敗する）。
     *
     * @param rec jOOQレコード
     * @return 招待Entity
     */
    Invitation toEntity(InvitationsRecord rec);

    /**
     * 招待Entityの値をjOOQレコードへ設定します（更新用）。
     *
     * @param entity 招待Entity
     * @param record jOOQレコード（書き込み先）
     */
    @Mapping(target = "id", ignore = true)
    void fillRecord(Invitation entity, @MappingTarget InvitationsRecord record);
}
