/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.http.controllers;

import com.authorization.domain.staff.enums.StaffRole;
import com.authorization.domain.staff.valueobjects.StaffListVo;
import com.authorization.domain.staff.valueobjects.StaffMutationVo;
import com.authorization.domain.staff.valueobjects.StaffRemoveVo;
import com.authorization.domain.staff.valueobjects.StaffResourceVo;
import com.authorization.support.http.responses.Pager;
import com.authorization.support.http.responses.ResponseHelper;
import com.authorization.usecases.staff.StaffService;
import com.authorization.usecases.staff.dtos.StaffDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * スタッフControllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@RestController
@RequestMapping("/api/staffs")
public class StaffController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final ObjectMapper JSON =
            new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final StaffService service;

    /**
     * コンストラクタ。
     *
     * @param service スタッフService
     */
    public StaffController(StaffService service) {
        this.service = service;
    }

    /**
     * スタッフ一覧を返します。
     *
     * @param keyword 検索キーワード
     * @param roles 権限フィルタ
     * @param sort ソート対象カラム
     * @param sortType ソート順
     * @param limit 取得件数
     * @param page ページ番号
     * @return JSON レスポンス
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Integer> roles,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "sort_type", required = false) String sortType,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "1") int page) {
        StaffDto dto = new StaffDto();
        dto.setKeyword(keyword);
        dto.setRoles(roles == null ? List.of() : roles);
        dto.setSort(sort == null ? "" : sort);
        dto.setSortType(com.authorization.support.enums.SortType.fromValue(sortType));
        dto.setLimit(limit);
        dto.setPaging(page);

        StaffListVo vo = service.index(dto);
        return ResponseHelper.success(toIndexJson(vo));
    }

    /**
     * スタッフの権限を更新します。
     *
     * @param id スタッフID
     * @param executorId 操作を実行したスタッフID（クッキーから取得）
     * @param body リクエストボディ
     * @return JSON レスポンス
     */
    @PatchMapping("/{id}/updateRole")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable long id,
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long executorId,
            @RequestBody Map<String, Object> body) {
        StaffDto dto = new StaffDto();
        dto.setId(id);
        dto.setExecutorId(executorId);
        Object roleObj = body.get("role");
        if (roleObj != null) {
            dto.setRole(StaffRole.from(((Number) roleObj).intValue()));
        }

        StaffMutationVo vo = service.updateRole(dto);
        return ResponseHelper.success(Map.of("id", vo.getId()));
    }

    /**
     * スタッフの論理削除を復元します。
     *
     * @param id スタッフID
     * @return JSON レスポンス
     */
    @PatchMapping("/{id}/restore")
    public ResponseEntity<Map<String, Object>> restore(@PathVariable long id) {
        StaffDto dto = new StaffDto();
        dto.setId(id);

        StaffRemoveVo vo = service.restore(dto);
        return ResponseHelper.success(Map.of("id", vo.getId()));
    }

    /**
     * スタッフを論理削除します。
     *
     * @param id スタッフID
     * @param executorId 操作を実行したスタッフID（クッキーから取得）
     * @param body リクエストボディ（version を含む）
     * @return JSON レスポンス
     */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Map<String, Object>> destroy(
            @PathVariable long id,
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long executorId,
            @RequestBody Map<String, Object> body) {
        StaffDto dto = new StaffDto();
        dto.setId(id);
        dto.setExecutorId(executorId);
        if (body.get("version") != null) {
            dto.setVersion(((Number) body.get("version")).intValue());
        }

        StaffRemoveVo vo = service.destroy(dto);
        return ResponseHelper.success(Map.of("id", vo.getId()));
    }

    /**
     * スタッフ一覧 ValueObject を JSON（{@code data}/{@code pager}）へ変換します。
     *
     * @param vo スタッフ一覧ValueObject
     * @return レスポンス本体
     */
    private static Map<String, Object> toIndexJson(StaffListVo vo) {
        List<Map<String, Object>> data = vo.getItems().stream().map(StaffController::toJson).toList();
        Pager pager = new Pager(vo.getCount(), vo.getOffset(), vo.getLimit(), data.size());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("data", data);
        res.put("pager", pager);
        return res;
    }

    /**
     * スタッフリソース ValueObject を JSON へ変換します。
     *
     * @param staff スタッフリソースValueObject
     * @return レスポンス用マップ
     */
    private static Map<String, Object> toJson(StaffResourceVo staff) {
        Map<String, Object> data = JSON.convertValue(staff, new TypeReference<Map<String, Object>>() {});
        data.remove("found");
        data.put("created_at", staff.getCreatedAt() != null ? staff.getCreatedAt().format(FMT) : null);
        data.put("updated_at", staff.getUpdatedAt() != null ? staff.getUpdatedAt().format(FMT) : null);
        return data;
    }
}
