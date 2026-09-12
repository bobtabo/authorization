/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.mappers;

import com.authorization.domain.client.entities.JwtHistory;
import com.authorization.jooq.tables.records.JwtHistoriesRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * JWT履歴 Entity と jOOQ Record を相互変換する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface JwtHistoryRecordMapper {

    /**
     * jOOQレコードをJWT履歴Entityへ変換します。createdBy/updatedAt/updatedBy/version は既存実装から
     * 引き続き設定しません（JWT履歴の表示ではこれらを使用しないため）。
     *
     * @param rec jOOQレコード
     * @return JWT履歴Entity
     */
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    JwtHistory toEntity(JwtHistoriesRecord rec);

    /**
     * JWT履歴Entityの値をjOOQレコードへ設定します（新規登録用）。createdAt/updatedAt/version が
     * 未設定の場合はそれぞれ現在時刻・現在時刻・1で補完します。
     *
     * @param entity JWT履歴Entity
     * @param record jOOQレコード（書き込み先）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(
            target = "createdAt",
            expression = "java(entity.getCreatedAt() != null ? entity.getCreatedAt() : java.time.LocalDateTime.now())")
    @Mapping(
            target = "updatedAt",
            expression = "java(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : java.time.LocalDateTime.now())")
    @Mapping(
            target = "version",
            expression = "java((long) (entity.getVersion() != null ? entity.getVersion() : 1))")
    void fillRecord(JwtHistory entity, @MappingTarget JwtHistoriesRecord record);
}
