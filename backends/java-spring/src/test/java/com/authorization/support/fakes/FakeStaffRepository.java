/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.fakes;

import com.authorization.domain.staff.condition.StaffCondition;
import com.authorization.domain.staff.entities.Staff;
import com.authorization.domain.staff.repositories.StaffRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * テスト用の手書きFakeスタッフRepositoryです（モックライブラリは使いません）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class FakeStaffRepository implements StaffRepository {

    private final Map<Long, Staff> staffs = new LinkedHashMap<>();
    private long nextId = 1;
    private int persistCallCount;
    private int deleteByIdCallCount;
    private int restoreByIdCallCount;

    /**
     * スタッフを追加します。id未設定の場合は自動採番します。
     *
     * @param staff 追加するスタッフEntity
     * @return このFake自身（メソッドチェーン用）
     */
    public FakeStaffRepository add(Staff staff) {
        if (staff.getId() == null) {
            staff.setId(nextId++);
        }
        staffs.put(staff.getId(), staff);
        return this;
    }

    /**
     * persist が呼ばれた回数を返します。
     *
     * @return 呼び出し回数
     */
    public int getPersistCallCount() {
        return persistCallCount;
    }

    /**
     * deleteById が呼ばれた回数を返します。
     *
     * @return 呼び出し回数
     */
    public int getDeleteByIdCallCount() {
        return deleteByIdCallCount;
    }

    /**
     * restoreById が呼ばれた回数を返します。
     *
     * @return 呼び出し回数
     */
    public int getRestoreByIdCallCount() {
        return restoreByIdCallCount;
    }

    /** {@inheritDoc} */
    @Override
    public List<Staff> findByCondition(StaffCondition condition) {
        return new ArrayList<>(staffs.values());
    }

    /** {@inheritDoc} */
    @Override
    public int countByCondition(StaffCondition condition) {
        return staffs.size();
    }

    /** {@inheritDoc} */
    @Override
    public Staff findById(StaffCondition condition) {
        Staff staff = staffs.get(condition.getId());
        return staff != null && staff.getDeletedAt() == null ? staff : null;
    }

    /** {@inheritDoc} */
    @Override
    public Staff findByProvider(StaffCondition condition) {
        return staffs.values().stream()
                .filter(staff -> staff.getDeletedAt() == null)
                .filter(staff -> staff.getProvider() == condition.getProvider())
                .filter(staff -> Objects.equals(staff.getProviderId(), condition.getProviderId()))
                .findFirst()
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public Staff persist(Staff entity) {
        persistCallCount++;
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        staffs.put(entity.getId(), entity);
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteById(Staff entity) {
        deleteByIdCallCount++;
        Staff existing = staffs.get(entity.getId());
        if (existing == null || !Objects.equals(existing.getVersion(), entity.getVersion())) {
            return false;
        }
        existing.setDeletedAt(entity.getDeletedAt());
        existing.setDeletedBy(entity.getDeletedBy());
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean restoreById(Staff entity) {
        restoreByIdCallCount++;
        Staff existing = staffs.get(entity.getId());
        if (existing == null || existing.getDeletedAt() == null) {
            return false;
        }
        existing.setDeletedAt(null);
        existing.setDeletedBy(null);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public List<Staff> findAllActive() {
        return staffs.values().stream().filter(staff -> staff.getDeletedAt() == null).toList();
    }
}
