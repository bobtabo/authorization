/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.http.controllers;

import com.authorization.domain.invitation.valueobjects.InvitationVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.http.responses.ResponseHelper;
import com.authorization.usecases.invitation.InvitationService;
import com.authorization.usecases.invitation.dtos.InvitationDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理招待Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@RestController
@RequestMapping("/api/admin/invitation")
public class AdminInvitationController {

    private static final ObjectMapper JSON =
            new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final InvitationService service;

    /**
     * コンストラクタ。
     *
     * @param service 招待Service
     */
    public AdminInvitationController(InvitationService service) {
        this.service = service;
    }

    /**
     * 現在の招待 URL を返します。
     *
     * @param role 権限（1=管理者, 2=メンバー。省略時は2）
     * @return JSON レスポンス
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> index(@RequestParam(required = false) String role) {
        InvitationDto dto = new InvitationDto();
        dto.setRole(resolveRole(role));

        InvitationVo vo = service.current(dto);
        return ResponseHelper.success(toJson(vo));
    }

    /**
     * 招待 URL を発行します。
     *
     * @param executorId staff_id クッキーの値
     * @param role 権限（1=管理者, 2=メンバー。省略時は2）
     * @return JSON レスポンス
     */
    @GetMapping("/issue")
    public ResponseEntity<Map<String, Object>> issue(
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long executorId,
            @RequestParam(required = false) String role) {
        if (executorId == 0L) {
            throw AppException.unauthorized("unauthenticated");
        }

        InvitationDto dto = new InvitationDto();
        dto.setRole(resolveRole(role));
        dto.setExecutorId(executorId);

        InvitationVo vo = service.issue(dto);
        return ResponseHelper.success(toJson(vo));
    }

    /**
     * クエリパラメーターから role を取得します（1 or 2、それ以外は 400）。
     *
     * @param raw role クエリパラメーターの生値
     * @return 権限（1=管理者, 2=メンバー）
     */
    private static int resolveRole(String raw) {
        if (raw == null) {
            return 2;
        }
        int role;
        try {
            role = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw AppException.badRequest("invalid_role");
        }
        if (role != 1 && role != 2) {
            throw AppException.badRequest("invalid_role");
        }
        return role;
    }

    /**
     * 招待 ValueObject を JSON へ変換します。
     *
     * @param invitation 招待ValueObject
     * @return レスポンス用マップ
     */
    private static Map<String, Object> toJson(InvitationVo invitation) {
        return JSON.convertValue(invitation, new TypeReference<Map<String, Object>>() {});
    }
}
