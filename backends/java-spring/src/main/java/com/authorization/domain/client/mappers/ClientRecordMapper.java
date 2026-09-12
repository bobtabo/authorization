/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.mappers;

import com.authorization.domain.client.entities.Client;
import com.authorization.jooq.tables.records.ClientsRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * クライアント Entity と jOOQ Record を相互変換する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface ClientRecordMapper {

    /**
     * jOOQレコードをクライアントEntityへ変換します。
     *
     * @param rec jOOQレコード
     * @return クライアントEntity
     */
    @Mapping(target = "building", expression = "java(rec.getBuilding() != null ? rec.getBuilding() : \"\")")
    @Mapping(
            target = "status",
            expression =
                    "java(rec.getStatus() == null ? null :"
                            + " com.authorization.domain.client.enums.ClientStatus.from(rec.getStatus().intValue()))")
    Client toEntity(ClientsRecord rec);

    /**
     * クライアントEntityの値をjOOQレコードへ設定します（新規登録用）。
     *
     * @param entity クライアントEntity
     * @param record jOOQレコード（書き込み先）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(
            target = "status",
            expression = "java(entity.getStatus() == null ? null : (long) entity.getStatus().value())")
    void fillRecord(Client entity, @MappingTarget ClientsRecord record);
}
