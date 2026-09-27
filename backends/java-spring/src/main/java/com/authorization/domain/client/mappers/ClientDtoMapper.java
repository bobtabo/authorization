/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.mappers;

import com.authorization.domain.client.entities.Client;
import com.authorization.usecases.client.dtos.ClientDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * クライアント DTO の値を Entity へ反映する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClientDtoMapper {

    /**
     * クライアントDTOの基本情報（氏名・住所・連絡先）をEntityへ反映します。
     * identifier/accessToken/status/version/idは対象外です
     * （生成規則や個別の遷移ロジックがあるため呼び出し側で制御します）。
     * DTOの値がnullのプロパティは上書きしないため、新規登録・部分更新の両方で使えます。
     *
     * @param dto クライアントDTO
     * @param entity 反映先のクライアントEntity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identifier", ignore = true)
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    void applyBasicInfo(ClientDto dto, @MappingTarget Client entity);
}
