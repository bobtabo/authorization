/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.client.dtos;

import com.authorization.domain.client.enums.ClientStatus;
import com.authorization.support.dtos.PagerDto;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * クライアントDTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
@Setter
public class ClientDto extends PagerDto {

    private Long id;
    private String keyword;
    private String startFrom;
    private String startTo;
    private List<Integer> statuses = List.of();
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
    private ClientStatus status;
}
