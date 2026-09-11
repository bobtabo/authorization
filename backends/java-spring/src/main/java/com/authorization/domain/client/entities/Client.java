/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.entities;

import com.authorization.domain.client.enums.ClientStatus;
import com.authorization.support.entities.AbstractEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * クライアントEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Client extends AbstractEntity {

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
    private String accessToken;
    private String privateKey;
    private String publicKey;
    private String fingerprint;
    private ClientStatus status;
    private LocalDateTime startAt;
    private LocalDateTime stopAt;
}
