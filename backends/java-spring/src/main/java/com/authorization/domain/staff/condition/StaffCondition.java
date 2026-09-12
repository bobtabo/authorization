/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.condition;

import com.authorization.domain.staff.enums.Provider;
import com.authorization.domain.staff.enums.StaffRole;
import com.authorization.domain.staff.enums.StaffStatus;
import com.authorization.support.repositories.conditions.AbstractCondition;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * スタッフConditionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffCondition extends AbstractCondition {

    private String name;
    private String email;
    private Provider provider;
    private String providerId;
    private String avatar;
    private StaffRole role;
    private StaffStatus status;
    private LocalDateTime lastLoginAt;

    /** 一覧検索用キーワード（名前・メールアドレスの部分一致）。 */
    private String keyword;

    /** 権限コードの一覧（空は無条件）。 */
    private List<Integer> roles = List.of();

    /** 状態コードの一覧（空は無条件）。 */
    private List<Integer> statuses = List.of();
}
