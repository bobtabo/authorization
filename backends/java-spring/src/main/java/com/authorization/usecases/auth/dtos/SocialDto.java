/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.auth.dtos;

import com.authorization.domain.staff.enums.Provider;
import com.authorization.support.dtos.AbstractDto;
import lombok.Getter;
import lombok.Setter;

/**
 * ソーシャルDTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
@Setter
public class SocialDto extends AbstractDto {

    private Provider provider;
    private String providerId;
    private String nickname;
    private String name;
    private String email;
    private String avatar;
    private String invitationToken;
}
