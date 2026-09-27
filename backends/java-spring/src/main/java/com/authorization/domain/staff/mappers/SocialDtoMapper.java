/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.mappers;

import com.authorization.domain.staff.entities.Staff;
import com.authorization.usecases.auth.dtos.SocialDto;
import org.mapstruct.Mapper;

/**
 * ソーシャル認証 DTO の値を スタッフ Entity へ反映する MapStruct マッパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Mapper(componentModel = "spring")
public interface SocialDtoMapper {

    /**
     * ソーシャル認証DTOからスタッフEntityを新規に組み立てます。
     *
     * @param dto ソーシャルDTO
     * @return スタッフEntity（未保存）
     */
    Staff toEntity(SocialDto dto);
}
