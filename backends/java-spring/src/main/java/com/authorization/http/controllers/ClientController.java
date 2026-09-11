/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.http.controllers;

import com.authorization.config.AppConfig;
import com.authorization.domain.client.entities.Client;
import com.authorization.domain.client.entities.JwtHistory;
import com.authorization.domain.client.enums.ClientStatus;
import com.authorization.domain.client.valueobjects.ClientDetailVo;
import com.authorization.domain.client.valueobjects.ClientInfoVo;
import com.authorization.domain.client.valueobjects.ClientListVo;
import com.authorization.domain.client.valueobjects.ClientQrVo;
import com.authorization.domain.client.valueobjects.ClientStartVo;
import com.authorization.domain.client.valueobjects.ClientStoreVo;
import com.authorization.infrastructure.mail.Mailer;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.http.responses.Pager;
import com.authorization.support.http.responses.ResponseHelper;
import com.authorization.usecases.client.ClientService;
import com.authorization.usecases.client.dtos.ClientDto;
import com.authorization.usecases.jwthistory.JwtHistoryService;
import com.authorization.usecases.jwthistory.dtos.JwtHistoryDto;
import com.authorization.usecases.notification.NotificationService;
import com.authorization.usecases.notification.dtos.NotificationCreateDto;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * クライアントControllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter FMT_SEC = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ClientService clientService;
    private final NotificationService notificationService;
    private final JwtHistoryService jwtHistoryService;
    private final Mailer mailer;
    private final AppConfig cfg;
    private final DSLContext dsl;

    public ClientController(
            ClientService clientService,
            NotificationService notificationService,
            JwtHistoryService jwtHistoryService,
            Mailer mailer,
            AppConfig cfg,
            DSLContext dsl) {
        this.clientService = clientService;
        this.notificationService = notificationService;
        this.jwtHistoryService = jwtHistoryService;
        this.mailer = mailer;
        this.cfg = cfg;
        this.dsl = dsl;
    }

    /**
     * クライアント一覧を検索して返します。message envelope は付けません（PHP版が
     * {@code response()->json()} を使うため）。
     *
     * @param keyword 検索キーワード
     * @param startFrom 利用開始日 From
     * @param startTo 利用開始日 To
     * @param limit 取得件数
     * @param page ページ番号
     * @param sort ソート対象カラム
     * @param sortType ソート順
     * @return JSON レスポンス
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> index(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "start_from", required = false) String startFrom,
            @RequestParam(name = "start_to", required = false) String startTo,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "sort_type", required = false) String sortType) {
        ClientDto dto = new ClientDto();
        dto.setKeyword(keyword);
        dto.setStartFrom(startFrom);
        dto.setStartTo(startTo);
        dto.setSort(sort == null ? "" : sort);
        dto.setSortType(com.authorization.support.enums.SortType.fromValue(sortType));
        dto.setLimit(limit);
        dto.setPaging(page);

        ClientListVo vo = clientService.getClients(dto);
        List<Map<String, Object>> data = vo.getClients().stream().map(ClientController::toListJson).toList();
        Pager pager = new Pager(vo.getCount(), vo.getOffset(), vo.getLimit(), data.size());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("data", data);
        res.put("pager", pager);
        return ResponseHelper.json(res);
    }

    /**
     * クライアント詳細を返します。
     *
     * @param id クライアントID
     * @return JSON レスポンス
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> show(@PathVariable long id) {
        ClientDto dto = new ClientDto();
        dto.setId(id);

        ClientDetailVo vo = clientService.show(dto);
        return ResponseHelper.success(toDetailJson(vo));
    }

    /**
     * クライアントを登録します。
     *
     * @param executorId staff_id クッキーの値
     * @param body リクエストボディ
     * @return JSON レスポンス
     */
    @PostMapping("/store")
    public ResponseEntity<Map<String, Object>> store(
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long executorId,
            @RequestBody Map<String, Object> body) {
        String name = str(body, "name");
        String postCode = str(body, "post_code");
        String pref = str(body, "pref");
        String city = str(body, "city");
        String address = str(body, "address");
        String building = (String) body.get("building");
        String tel = str(body, "tel");
        String email = str(body, "email");

        if (!com.authorization.http.requests.ClientValidation.validateStore(
                name, postCode, pref, city, address, building, tel, email)) {
            return ResponseEntity.status(422).body(Map.of("message", "validation_error"));
        }

        ClientDto dto = new ClientDto();
        dto.setName(name);
        dto.setPostCode(postCode);
        dto.setPref(pref);
        dto.setCity(city);
        dto.setAddress(address);
        dto.setBuilding(building);
        dto.setTel(tel);
        dto.setEmail(email);
        dto.setExecutorId(executorId);

        ClientStoreVo vo = dsl.transactionResult(config -> {
            ClientStoreVo storeVo = clientService.store(dto);

            NotificationCreateDto notificationDto = new NotificationCreateDto();
            notificationDto.setMessageType(1);
            notificationDto.setTitle("新しいクライアントが登録されました");
            notificationDto.setMessage(storeVo.getName() != null ? storeVo.getName() : "");
            notificationDto.setUrl("/clients/show?id=" + storeVo.getId());
            notificationDto.setExecutorId(executorId);
            notificationService.fanOut(notificationDto);

            return storeVo;
        });

        String activateUrl = cfg.app().frontendUrl() + "/clients/" + vo.getIdentifier() + "/qr";
        Thread.ofVirtual().start(() -> mailer.sendActivation(vo.getEmail(), vo.getName(), activateUrl));

        return ResponseHelper.success(toStoreJson(vo), HttpStatus.CREATED.value());
    }

    /**
     * クライアントを更新します。
     *
     * @param id クライアントID
     * @param executorId staff_id クッキーの値
     * @param body リクエストボディ
     * @return JSON レスポンス
     */
    @PutMapping("/{id}/update")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable long id,
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long executorId,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String postCode = (String) body.get("post_code");
        String pref = (String) body.get("pref");
        String city = (String) body.get("city");
        String address = (String) body.get("address");
        String building = (String) body.get("building");
        String tel = (String) body.get("tel");
        String email = (String) body.get("email");
        Integer status = body.get("status") != null ? ((Number) body.get("status")).intValue() : null;
        Integer version = body.get("version") != null ? ((Number) body.get("version")).intValue() : null;

        if (!com.authorization.http.requests.ClientValidation.validateUpdate(
                name, postCode, pref, city, address, building, tel, email)) {
            return ResponseEntity.status(422).body(Map.of("message", "validation_error"));
        }

        ClientDto dto = new ClientDto();
        dto.setId(id);
        dto.setName(name);
        dto.setPostCode(postCode);
        dto.setPref(pref);
        dto.setCity(city);
        dto.setAddress(address);
        dto.setBuilding(building);
        dto.setTel(tel);
        dto.setEmail(email);
        dto.setStatus(status == null ? null : ClientStatus.from(status));
        dto.setVersion(version);
        dto.setExecutorId(executorId);

        ClientStoreVo vo = dsl.transactionResult(config -> clientService.update(dto));
        return ResponseHelper.success(toStoreJson(vo));
    }

    /**
     * スマホ連携用QRコードデータを返します。
     *
     * @param identifier クライアント識別子
     * @return JSON レスポンス
     */
    @GetMapping("/{identifier}/qr")
    public ResponseEntity<Map<String, Object>> qr(@PathVariable String identifier) {
        ClientDto dto = new ClientDto();
        dto.setIdentifier(identifier);

        ClientQrVo vo = clientService.getQr(dto);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("identifier", vo.getIdentifier());
        data.put("deeplink_url", vo.getDeeplinkUrl());
        return ResponseHelper.success(data);
    }

    /**
     * スマホアプリからの利用開始を処理し、アクセストークンを返します。
     *
     * @param identifier クライアント識別子
     * @return JSON レスポンス
     */
    @PatchMapping("/{identifier}/start")
    public ResponseEntity<Map<String, Object>> start(@PathVariable String identifier) {
        ClientDto dto = new ClientDto();
        dto.setIdentifier(identifier);

        ClientStartVo vo = dsl.transactionResult(config -> clientService.start(dto));
        return ResponseHelper.success(Map.of("access_token", vo.getAccessToken()));
    }

    /**
     * スマホアプリからの利用停止を処理します。
     *
     * @param identifier クライアント識別子
     * @return JSON レスポンス
     */
    @PatchMapping("/{identifier}/stop")
    public ResponseEntity<Map<String, Object>> stop(@PathVariable String identifier) {
        ClientDto dto = new ClientDto();
        dto.setIdentifier(identifier);

        dsl.transaction(config -> clientService.stop(dto));
        return ResponseHelper.success(Map.of());
    }

    /**
     * スマホアプリ向けにクライアント情報を返します。
     *
     * @param identifier クライアント識別子
     * @return JSON レスポンス
     */
    @GetMapping("/{identifier}/info")
    public ResponseEntity<Map<String, Object>> info(@PathVariable String identifier) {
        ClientDto dto = new ClientDto();
        dto.setIdentifier(identifier);

        ClientInfoVo vo = clientService.getInfo(dto);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("identifier", vo.getIdentifier());
        data.put("name", vo.getName());
        data.put("status", vo.getStatus());
        return ResponseHelper.success(data);
    }

    /**
     * クライアントに紐づくJWT履歴一覧を返します。message envelope は付けません
     * （PHP版が {@code response()->json()} を使うため）。
     *
     * @param id クライアントID
     * @param limit 取得件数
     * @param page ページ番号
     * @param sort ソート対象カラム
     * @param sortType ソート順
     * @return JSON レスポンス
     */
    @GetMapping("/{id}/jwt-histories")
    public ResponseEntity<Map<String, Object>> jwtHistories(
            @PathVariable long id,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, name = "sort_type") String sortType) {
        JwtHistoryDto dto = new JwtHistoryDto();
        dto.setClientId(id);
        dto.setSort(sort == null ? "" : sort);
        dto.setSortType(com.authorization.support.enums.SortType.fromValue(sortType));
        dto.setLimit(limit);
        dto.setPaging(page);

        var vo = jwtHistoryService.getHistories(dto);
        List<Map<String, Object>> data = vo.getItems().stream().map(ClientController::toHistoryJson).toList();
        Pager pager = new Pager(vo.getCount(), vo.getOffset(), vo.getLimit(), data.size());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("data", data);
        res.put("pager", pager);
        return ResponseHelper.json(res);
    }

    /**
     * クライアントを論理削除します。
     *
     * @param id クライアントID
     * @param executorId staff_id クッキーの値
     * @param body リクエストボディ（version を含む）
     * @return JSON レスポンス
     */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Map<String, Object>> destroy(
            @PathVariable long id,
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long executorId,
            @RequestBody Map<String, Object> body) {
        ClientDto dto = new ClientDto();
        dto.setId(id);
        dto.setExecutorId(executorId);
        if (body.get("version") != null) {
            dto.setVersion(((Number) body.get("version")).intValue());
        } else {
            throw AppException.badRequest("version_required");
        }

        dsl.transaction(config -> clientService.destroy(dto));
        return ResponseHelper.success(Map.of());
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v != null ? v.toString() : "";
    }

    /**
     * クライアント Entity を一覧行の JSON に変換します。
     *
     * @param c クライアント Entity
     * @return レスポンス用マップ
     */
    private static Map<String, Object> toListJson(Client c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("status", c.getStatus().value());
        m.put("start_at", c.getStartAt() != null ? c.getStartAt().format(FMT) : null);
        m.put("stop_at", c.getStopAt() != null ? c.getStopAt().format(FMT) : null);
        m.put("created_at", c.getCreatedAt().format(FMT));
        m.put("updated_at", c.getUpdatedAt().format(FMT));
        return m;
    }

    /**
     * クライアント詳細 ValueObject を JSON へ変換します。
     *
     * @param v クライアント詳細ValueObject
     * @return レスポンス用マップ
     */
    private static Map<String, Object> toDetailJson(ClientDetailVo v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("name", v.getName());
        m.put("identifier", v.getIdentifier());
        m.put("post_code", v.getPostCode());
        m.put("pref", v.getPref());
        m.put("city", v.getCity());
        m.put("address", v.getAddress());
        m.put("building", v.getBuilding());
        m.put("tel", v.getTel());
        m.put("email", v.getEmail());
        m.put("status", v.getStatus());
        m.put("start_at", v.getStartAt() != null ? v.getStartAt().format(FMT) : "");
        m.put("stop_at", v.getStopAt() != null ? v.getStopAt().format(FMT) : "");
        m.put("created_at", v.getCreatedAt() != null ? v.getCreatedAt().format(FMT) : "");
        m.put("updated_at", v.getUpdatedAt() != null ? v.getUpdatedAt().format(FMT) : "");
        return m;
    }

    /**
     * クライアント登録・更新 ValueObject を JSON へ変換します。
     *
     * @param v クライアント登録・更新ValueObject
     * @return レスポンス用マップ
     */
    private static Map<String, Object> toStoreJson(ClientStoreVo v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("name", v.getName());
        m.put("identifier", v.getIdentifier());
        m.put("post_code", v.getPostCode());
        m.put("pref", v.getPref());
        m.put("city", v.getCity());
        m.put("address", v.getAddress());
        m.put("building", v.getBuilding());
        m.put("tel", v.getTel());
        m.put("email", v.getEmail());
        m.put("status", v.getStatus());
        m.put("start_at", v.getStartAt() != null ? v.getStartAt().format(FMT) : "");
        m.put("stop_at", v.getStopAt() != null ? v.getStopAt().format(FMT) : "");
        m.put("created_at", v.getCreatedAt() != null ? v.getCreatedAt().format(FMT) : "");
        m.put("updated_at", v.getUpdatedAt() != null ? v.getUpdatedAt().format(FMT) : "");
        return m;
    }

    /**
     * JWT履歴 Entity を JSON へ変換します。
     *
     * @param h JWT履歴Entity
     * @return レスポンス用マップ
     */
    private static Map<String, Object> toHistoryJson(JwtHistory h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", h.getId());
        m.put("member_id", h.getMemberId());
        m.put("issue_at", h.getIssueAt().format(FMT_SEC));
        m.put("jwt", h.getJwt());
        return m;
    }
}
