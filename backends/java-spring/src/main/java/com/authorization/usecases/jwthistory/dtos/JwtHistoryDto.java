/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.jwthistory.dtos;

import com.authorization.support.dtos.PagerDto;
import lombok.Getter;
import lombok.Setter;

/**
 * JWT履歴DTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
@Setter
public class JwtHistoryDto extends PagerDto {

    private Long clientId;
}
