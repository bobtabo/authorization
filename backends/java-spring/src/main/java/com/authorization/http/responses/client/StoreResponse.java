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
     * @param store クライアント登録・更新ValueObject
     */
    public StoreResponse(ClientStoreVo store) {
        this.id = store.getId();
        this.name = store.getName();
        this.identifier = store.getIdentifier();
        this.postCode = store.getPostCode();
        this.pref = store.getPref();
        this.city = store.getCity();
        this.address = store.getAddress();
        this.building = store.getBuilding();
        this.tel = store.getTel();
        this.email = store.getEmail();
        this.status = store.getStatus();
        this.startAt = store.getStartAt() != null ? store.getStartAt().format(FMT) : "";
        this.stopAt = store.getStopAt() != null ? store.getStopAt().format(FMT) : "";
        this.createdAt = store.getCreatedAt() != null ? store.getCreatedAt().format(FMT) : "";
        this.updatedAt = store.getUpdatedAt() != null ? store.getUpdatedAt().format(FMT) : "";
    }
}
