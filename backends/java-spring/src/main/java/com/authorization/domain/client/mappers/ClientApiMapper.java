/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.mappers;

import com.authorization.domain.client.entities.Client;
import com.authorization.domain.client.valueobjects.ClientDetailVo;
import com.authorization.domain.client.valueobjects.ClientStoreVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * クライアント Entity を API 向け ValueObject へ変換する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface ClientApiMapper {

    /**
     * クライアント Entity を詳細 ValueObject に変換します。
     *
     * @param client クライアント Entity
     * @return クライアント詳細 ValueObject
     */
    @Mapping(target = "status", expression = "java(client.getStatus() == null ? null : client.getStatus().value())")
    ClientDetailVo toDetailVo(Client client);

    /**
     * クライアント Entity を登録・更新結果 ValueObject に変換します。
     *
     * @param client クライアント Entity
     * @return クライアント登録・更新 ValueObject
     */
    @Mapping(target = "status", expression = "java(client.getStatus() == null ? null : client.getStatus().value())")
    ClientStoreVo toStoreVo(Client client);
}
