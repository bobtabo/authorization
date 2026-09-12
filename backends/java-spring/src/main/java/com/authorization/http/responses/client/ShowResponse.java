/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.http.responses.client;

import com.authorization.domain.client.valueobjects.ClientDetailVo;
import com.authorization.support.http.responses.AbstractResponse;
import java.time.format.DateTimeFormatter;

/**
 * クライアント詳細Responseクラスです（PHP版 ShowResponse 相当）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class ShowResponse extends AbstractResponse {

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
     * @param detail クライアント詳細ValueObject
     */
    public ShowResponse(ClientDetailVo detail) {
        this.id = detail.getId();
        this.name = detail.getName();
        this.identifier = detail.getIdentifier();
        this.postCode = detail.getPostCode();
        this.pref = detail.getPref();
        this.city = detail.getCity();
        this.address = detail.getAddress();
        this.building = detail.getBuilding();
        this.tel = detail.getTel();
        this.email = detail.getEmail();
        this.status = detail.getStatus();
        this.startAt = detail.getStartAt() != null ? detail.getStartAt().format(FMT) : "";
        this.stopAt = detail.getStopAt() != null ? detail.getStopAt().format(FMT) : "";
        this.createdAt = detail.getCreatedAt() != null ? detail.getCreatedAt().format(FMT) : "";
        this.updatedAt = detail.getUpdatedAt() != null ? detail.getUpdatedAt().format(FMT) : "";
    }
}
