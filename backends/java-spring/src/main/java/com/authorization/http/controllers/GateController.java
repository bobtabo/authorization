/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.http.controllers;

import com.authorization.domain.gate.valueobjects.GateVerifyVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.http.responses.ResponseHelper;
import com.authorization.usecases.gate.GateService;
import com.authorization.usecases.gate.dtos.GateIssueDto;
import com.authorization.usecases.gate.dtos.GateVerifyDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 認可Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@RestController
@RequestMapping("/api/gate")
public class GateController {

    // AbstractValueObject由来のversion（GateVerifyVoでは未使用のため常にnull）を
    // 出力に含めないよう、NON_NULLを設定する。
    private static final ObjectMapper JSON =
            new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private final GateService service;

    /**
     * コンストラクタ。
     *
     * @param service 認可Service
     */
    public GateController(GateService service) {
        this.service = service;
    }

    /**
     * クライアント会員向け JWT を発行する応答を返します。
     *
     * @param member クライアント会員ID
     * @param authorization Authorization ヘッダー（Bearer アクセストークン）
     * @return JSON レスポンス
     */
    @GetMapping("/issue")
    public ResponseEntity<Map<String, Object>> issue(
            @RequestParam String member,
            @RequestHeader(name = "Authorization", required = false, defaultValue = "") String authorization) {
        if (member == null || member.isEmpty()) {
            throw AppException.badRequest("member_required");
        }

        GateIssueDto dto = new GateIssueDto();
        dto.setMemberId(member);
        dto.setAccessToken(authorization.startsWith("Bearer ") ? authorization.substring(7) : "");

        var vo = service.issueToken(dto);
        return ResponseHelper.success(Map.of("token", vo.getToken()));
    }

    /**
     * JWT を検証し Payload 相当の応答を返します。
     *
     * @param identifier クライアント識別名
     * @param token 検証対象のJWT
     * @return JSON レスポンス
     */
    @GetMapping("/client/{identifier}/verify")
    public ResponseEntity<Map<String, Object>> verify(@PathVariable String identifier, @RequestParam String token) {
        if (token == null || token.isEmpty()) {
            throw AppException.badRequest("token_required");
        }
        if (identifier == null || identifier.isEmpty()) {
            throw AppException.badRequest("identifier_required");
        }

        GateVerifyDto dto = new GateVerifyDto();
        dto.setIdentifier(identifier);
        dto.setToken(token);

        GateVerifyVo vo = service.verify(dto);

        Map<String, Object> data = JSON.convertValue(vo, new TypeReference<Map<String, Object>>() {});
        return ResponseHelper.success(data);
    }
}
