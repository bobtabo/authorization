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

/**
 * 招待 Entity と jOOQ Record を相互変換する MapStruct マッパーです。招待は事前投入済みの行を
 * 更新するだけで新規作成しないため、fillRecord は用意していません。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface InvitationRecordMapper {

    /**
     * jOOQレコードを招待Entityへ変換します。createdBy/updatedBy/deletedAt/deletedBy は既存実装から
     * 引き続き設定しません（招待情報の表示ではこれらを使用しないため）。
     *
     * @param rec jOOQレコード
     * @return 招待Entity
     */
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Invitation toEntity(InvitationsRecord rec);
}
