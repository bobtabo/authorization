/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.auth.dtos;

import com.authorization.support.dtos.AbstractDto;
import lombok.Getter;
import lombok.Setter;

/**
 * 認証ユーザーDTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
@Setter
public class AuthUserDto extends AbstractDto {

    private Long id;
}
