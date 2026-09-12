/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.http.controllers;

import com.authorization.domain.notification.entities.Notification;
import com.authorization.domain.notification.valueobjects.NotificationCountsVo;
import com.authorization.domain.notification.valueobjects.NotificationListVo;
import com.authorization.domain.notification.valueobjects.NotificationSaveVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.http.responses.ResponseHelper;
import com.authorization.usecases.notification.NotificationService;
import com.authorization.usecases.notification.dtos.NotificationDto;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_DATE_TIME;

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    /**
     * staff_id クッキーからスタッフIDを取得します。未認証の場合は例外を投げます。
     *
     * @param staffId staff_id クッキーの値
     * @return スタッフID
     */
    private static long requireStaffId(long staffId) {
        if (staffId == 0L) {
            throw AppException.unauthorized("unauthenticated");
        }
        return staffId;
    }

    /**
     * 通知一覧（カーソルページング）を返します。
     *
     * @param staffId staff_id クッキーの値
     * @param cursor ページカーソル
     * @param limit 取得件数
     * @return JSON レスポンス
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> index(
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long staffId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {
        requireStaffId(staffId);

        NotificationDto dto = new NotificationDto();
        dto.setStaffId(staffId);
        dto.setCursor(cursor == null || cursor.isEmpty() ? null : cursor);
        dto.setLimit(limit != null && limit >= 1 ? limit : 1);

        NotificationListVo vo = service.listPage(dto);

        List<Map<String, Object>> items = vo.getItems().stream().map(NotificationController::toJson).toList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);
        data.put("next_cursor", vo.getNextCursor());
        return ResponseHelper.success(data);
    }

    /**
     * 通知の一括更新（既読など）の応答を返します。
     *
     * @param staffId staff_id クッキーの値
     * @return JSON レスポンス
     */
    @PatchMapping
    public ResponseEntity<Map<String, Object>> readAll(
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long staffId) {
        requireStaffId(staffId);

        NotificationDto dto = new NotificationDto();
        dto.setStaffId(staffId);
        dto.setAll(true);

        NotificationSaveVo vo = service.reads(dto);
        return ResponseHelper.success(Map.of("updated", vo.getUpdated()));
    }

    /**
     * 通知件数の集計を返します。
     *
     * @param staffId staff_id クッキーの値
     * @return JSON レスポンス
     */
    @GetMapping("/counts")
    public ResponseEntity<Map<String, Object>> counts(
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long staffId) {
        requireStaffId(staffId);

        NotificationDto dto = new NotificationDto();
        dto.setStaffId(staffId);

        NotificationCountsVo vo = service.counts(dto);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("unread", vo.getUnread());
        data.put("total", vo.getTotal());
        return ResponseHelper.success(data);
    }

    /**
     * 単一通知を更新する応答を返します。
     *
     * @param id 通知ID
     * @param staffId staff_id クッキーの値
     * @return JSON レスポンス
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> read(
            @PathVariable long id,
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long staffId) {
        requireStaffId(staffId);

        NotificationDto dto = new NotificationDto();
        dto.setStaffId(staffId);
        dto.setNotificationId(id);

        NotificationSaveVo vo = service.read(dto);
        return ResponseHelper.success(Map.of("id", vo.getId()));
    }

    /**
     * 通知エンティティをレスポンス用マップへ変換します。
     *
     * @param n 通知エンティティ
     * @return レスポンス用マップ
     */
    private static Map<String, Object> toJson(Notification n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", n.getId());
        m.put("staff_id", n.getStaffId());
        m.put("message_type", n.getMessageType());
        m.put("title", n.getTitle());
        m.put("message", n.getMessage());
        m.put("url", n.getUrl());
        m.put("read", n.isRead());
        m.put("created_at", n.getCreatedAt() != null ? n.getCreatedAt().format(FMT) : null);
        m.put("updated_at", n.getUpdatedAt() != null ? n.getUpdatedAt().format(FMT) : null);
        return m;
    }
}
