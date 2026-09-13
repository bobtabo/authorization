/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.mappers;

import com.authorization.domain.client.condition.ClientCondition;
import com.authorization.usecases.client.dtos.ClientDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * クライアント DTO を Condition へ変換する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface ClientConditionMapper {

    /**
     * クライアント DTO を検索条件へ変換します。startFrom/startTo は文字列から日時への変換が
     * 必要なため対象外とし、呼び出し側（Service）で個別に設定します。
     *
     * @param dto クライアント DTO
     * @return クライアント検索条件
     */
    @Mapping(target = "startFrom", ignore = true)
    @Mapping(target = "startTo", ignore = true)
    ClientCondition toCondition(ClientDto dto);
}
