/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.entities;

import com.authorization.support.entities.AbstractEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * JWT履歴Entityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JwtHistory extends AbstractEntity {

    private Long id;
    private Long clientId;
    private String memberId;
    private LocalDateTime issueAt;
    private String jwt;
}
