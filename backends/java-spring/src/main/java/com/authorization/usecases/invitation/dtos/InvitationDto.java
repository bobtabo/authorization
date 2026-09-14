/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.invitation.dtos;

import com.authorization.support.dtos.AbstractDto;
import lombok.Getter;
import lombok.Setter;

/**
 * 招待DTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
@Setter
public class InvitationDto extends AbstractDto {

    private String token;
    private Integer role;
}
