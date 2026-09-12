/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.http.responses.client;

import com.authorization.domain.client.valueobjects.ClientStoreVo;
import com.authorization.support.http.responses.AbstractResponse;
import java.time.format.DateTimeFormatter;

/**
 * クライアント登録・更新Responseクラスです（PHP版 StoreResponse 相当）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class StoreResponse extends AbstractResponse {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_DATE_TIME;

    private final Long id;
    private final String name;
    private final String identifier;
    private final String postCode;
    private final String pref;
    private final String city;
    private final String address;
    private final String building;
    private final String tel;
    private final String email;
    private final Integer status;
    private final String startAt;
    private final String stopAt;
    private final String createdAt;
    private final String updatedAt;

    /**
     * コンストラクタ。
     *
     * @param v クライアント登録・更新ValueObject
     */
    public StoreResponse(ClientStoreVo v) {
        this.id = v.getId();
        this.name = v.getName();
        this.identifier = v.getIdentifier();
        this.postCode = v.getPostCode();
        this.pref = v.getPref();
        this.city = v.getCity();
        this.address = v.getAddress();
        this.building = v.getBuilding();
        this.tel = v.getTel();
        this.email = v.getEmail();
        this.status = v.getStatus();
        this.startAt = v.getStartAt() != null ? v.getStartAt().format(FMT) : "";
        this.stopAt = v.getStopAt() != null ? v.getStopAt().format(FMT) : "";
        this.createdAt = v.getCreatedAt() != null ? v.getCreatedAt().format(FMT) : "";
        this.updatedAt = v.getUpdatedAt() != null ? v.getUpdatedAt().format(FMT) : "";
    }
}
