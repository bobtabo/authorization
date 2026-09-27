/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.fakes;

import com.authorization.domain.staff.condition.StaffCondition;
import com.authorization.domain.staff.entities.Staff;
import com.authorization.domain.staff.repositories.StaffRepository;
import com.authorization.support.repositories.conditions.Option;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

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
    private boolean persistFails;

    /**
     * persist 呼び出し時に例外を投げるようにします。
     *
     * @return このFake自身（メソッドチェーン用）
     */
    public FakeStaffRepository failOnPersist() {
        this.persistFails = true;
        return this;
    }

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
        Stream<Staff> stream = filtered(condition);
        Option option = condition.getOption();
        if (option != null) {
            String column = option.getOrderBy() != null ? option.getOrderBy() : option.getOrderByDesc();
            boolean desc = option.getOrderByDesc() != null;
            stream = stream.sorted(sortComparator(column, desc));
            if (condition.isPaging()) {
                int limit = (int) Math.clamp(option.getLimit(), 1, 500);
                stream = stream.skip(option.getOffset()).limit(limit);
            }
        }
        return stream.toList();
    }

    /** {@inheritDoc} */
    @Override
    public int countByCondition(StaffCondition condition) {
        return (int) filtered(condition).count();
    }

    /**
     * keyword/roles による絞り込みを適用します（{@code JooqStaffRepository.buildCondition}と
     * 同じ条件。論理削除済みも除外しません）。
     *
     * @param condition 検索条件
     * @return 絞り込み済みのスタッフストリーム
     */
    private Stream<Staff> filtered(StaffCondition condition) {
        Stream<Staff> stream = staffs.values().stream();
        if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
            String keyword = condition.getKeyword();
            stream = stream.filter(staff -> staff.getName().contains(keyword) || staff.getEmail().contains(keyword));
        }
        if (!condition.getRoles().isEmpty()) {
            stream = stream.filter(staff -> condition.getRoles().contains(staff.getRole().value()));
        }
        return stream;
    }

    /**
     * {@code JooqStaffRepository.findByCondition}と同じ並び替え対象カラムの分岐を再現します。
     *
     * @param column 並び替え対象カラム名
     * @param desc 降順の場合 true
     * @return 比較器
     */
    private static Comparator<Staff> sortComparator(String column, boolean desc) {
        Comparator<Staff> comparator = switch (column == null ? "" : column) {
            case "name" -> Comparator.comparing(Staff::getName);
            case "role" -> Comparator.comparing(staff -> staff.getRole().value());
            case "status" -> Comparator.comparing(Staff::getDeletedAt, Comparator.nullsFirst(Comparator.naturalOrder()));
            default -> Comparator.comparing(Staff::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder()));
        };
        return desc ? comparator.reversed() : comparator;
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
        if (persistFails) {
            throw new RuntimeException("db save failed");
        }
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
