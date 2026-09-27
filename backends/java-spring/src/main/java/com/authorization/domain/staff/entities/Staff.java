/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.domain.staff.entities;

import com.authorization.domain.staff.enums.Provider;
import com.authorization.domain.staff.enums.StaffRole;
import com.authorization.domain.staff.enums.StaffStatus;
import com.authorization.support.entities.AbstractEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * スタッフEntityクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Staff extends AbstractEntity {

    private Long id;
    private String name;
    private String email;
    private Provider provider;
    private String providerId;
    private String avatar;
    private StaffRole role;
    private StaffStatus status;
    private LocalDateTime lastLoginAt;
}
