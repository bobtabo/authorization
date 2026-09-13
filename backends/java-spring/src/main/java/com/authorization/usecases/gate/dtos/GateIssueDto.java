/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.gate.dtos;

import com.authorization.support.dtos.AbstractDto;
import lombok.Getter;
import lombok.Setter;

/**
 * JWT発行DTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
@Setter
public class GateIssueDto extends AbstractDto {

    private String memberId = "";
    private String accessToken = "";
}
