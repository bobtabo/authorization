/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.staff.dtos;

import com.authorization.domain.staff.enums.StaffRole;
import com.authorization.support.dtos.PagerDto;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * スタッフ API 用の入力 DTO です（一覧・権限更新・削除・単体取得で共用します）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
@Setter
public class StaffDto extends PagerDto {

    private Long id;
    private String keyword;

    /** 一覧検索用の権限コード（複数）。空リストは無条件。 */
    private List<Integer> roles = List.of();

    /** 一覧検索用の状態コード（複数）。空リストは無条件。 */
    private List<Integer> statuses = List.of();

    /** 権限更新時の権限コード。 */
    private StaffRole role;
}
