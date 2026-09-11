/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.valueobjects;

import com.authorization.support.valueobjects.AbstractValueObject;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * クライアント詳細ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientDetailVo extends AbstractValueObject {

    private Long id;
    private String name;
    private String identifier;
    private String postCode;
    private String pref;
    private String city;
    private String address;
    private String building;
    private String tel;
    private String email;
    private Integer status;
    private LocalDateTime startAt;
    private LocalDateTime stopAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
