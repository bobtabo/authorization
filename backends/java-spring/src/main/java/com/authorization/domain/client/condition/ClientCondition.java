/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.client.condition;

import com.authorization.domain.client.enums.ClientStatus;
import com.authorization.support.repositories.conditions.AbstractCondition;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * クライアントConditionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientCondition extends AbstractCondition {

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

    /** 一覧検索用キーワード（名前・識別子の部分一致）。 */
    private String keyword;

    /** 利用開始日 From（空は無条件）。 */
    private LocalDateTime startFrom;

    /** 利用開始日 To（空は無条件）。 */
    private LocalDateTime startTo;

    /** 状態コードの一覧（空は無条件）。 */
    private List<Integer> statuses = List.of();
}
