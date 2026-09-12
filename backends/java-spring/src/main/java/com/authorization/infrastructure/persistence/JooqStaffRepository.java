/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.persistence;

import static com.authorization.jooq.Tables.STAFFS;

import com.authorization.domain.staff.condition.StaffCondition;
import com.authorization.domain.staff.entities.Staff;
import com.authorization.domain.staff.enums.Provider;
import com.authorization.domain.staff.enums.StaffRole;
import com.authorization.domain.staff.enums.StaffStatus;
import com.authorization.domain.staff.repositories.StaffRepository;
import com.authorization.support.exceptions.AppException;
import java.time.LocalDateTime;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.SortOrder;
import org.springframework.stereotype.Component;

/**
 * jOOQ を使ったスタッフRepositoryの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Component
public class JooqStaffRepository implements StaffRepository {

    private final DSLContext dsl;

    /**
     * コンストラクタ。
     *
     * @param dsl jOOQ DSLContext
     */
    public JooqStaffRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByCondition(StaffCondition condition) {
        return dsl.fetchCount(applyFilters(condition));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Staff> findByCondition(StaffCondition condition) {
        SelectQuery<Record> q = applyFilters(condition);
        var option = condition.getOption();
        if (option != null) {
            String column = option.getOrderBy() != null ? option.getOrderBy() : option.getOrderByDesc();
            var sortField = switch (column == null ? "" : column) {
                case "name" -> STAFFS.NAME;
                case "role" -> STAFFS.ROLE;
                case "status" -> STAFFS.DELETED_AT;
                default -> STAFFS.CREATED_AT;
            };
            q.addOrderBy(sortField.sort(option.getOrderByDesc() != null ? SortOrder.DESC : SortOrder.ASC));
            if (condition.isPaging()) {
                q.addLimit(option.getOffset(), Math.clamp(option.getLimit(), 1, 500));
            }
        }
        return q.fetch().map(JooqStaffRepository::toEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Staff findById(StaffCondition condition) {
        Record rec = dsl.selectFrom(STAFFS)
                .where(STAFFS.ID.eq(condition.getId()))
                .and(STAFFS.DELETED_AT.isNull())
                .fetchOne();
        return rec == null ? null : toEntity(rec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Staff findByProvider(StaffCondition condition) {
        Record rec = dsl.selectFrom(STAFFS)
                .where(STAFFS.PROVIDER.eq(condition.getProvider().value()))
                .and(STAFFS.PROVIDER_ID.eq(condition.getProviderId()))
                .and(STAFFS.DELETED_AT.isNull())
                .fetchOne();
        return rec == null ? null : toEntity(rec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Staff> findAllActive() {
        return dsl.selectFrom(STAFFS).where(STAFFS.DELETED_AT.isNull()).fetch().map(JooqStaffRepository::toEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Staff persist(Staff entity) {
        if (entity.getId() == null) {
            var r = dsl.newRecord(STAFFS);
            r.setName(entity.getName());
            r.setEmail(entity.getEmail());
            r.setProvider(entity.getProvider().value());
            r.setProviderId(entity.getProviderId());
            r.setAvatar(entity.getAvatar());
            r.setRole((long) entity.getRole().value());
            r.setLastLoginAt(entity.getLastLoginAt());
            r.setCreatedAt(entity.getCreatedAt());
            r.setCreatedBy(entity.getCreatedBy());
            r.setUpdatedAt(entity.getUpdatedAt());
            r.setUpdatedBy(entity.getUpdatedBy());
            r.setVersion((long) entity.getVersion());
            r.store();
            entity.setId(r.getId());
            return entity;
        }

        int rows = dsl.update(STAFFS)
                .set(STAFFS.NAME, entity.getName())
                .set(STAFFS.EMAIL, entity.getEmail())
                .set(STAFFS.AVATAR, entity.getAvatar())
                .set(STAFFS.ROLE, (long) entity.getRole().value())
                .set(STAFFS.LAST_LOGIN_AT, entity.getLastLoginAt())
                .set(STAFFS.UPDATED_AT, entity.getUpdatedAt())
                .set(STAFFS.UPDATED_BY, entity.getUpdatedBy())
                .set(STAFFS.VERSION, (long) (entity.getVersion() + 1))
                .where(STAFFS.ID.eq(entity.getId()))
                .and(STAFFS.VERSION.eq((long) entity.getVersion()))
                .execute();
        if (rows == 0) {
            throw new AppException(409, "optimistic_lock");
        }
        entity.setVersion(entity.getVersion() + 1);
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteById(Staff entity) {
        int rows = dsl.update(STAFFS)
                .set(STAFFS.DELETED_AT, entity.getDeletedAt())
                .set(STAFFS.DELETED_BY, entity.getDeletedBy())
                .set(STAFFS.UPDATED_AT, entity.getDeletedAt())
                .set(STAFFS.UPDATED_BY, entity.getDeletedBy())
                .where(STAFFS.ID.eq(entity.getId()))
                .and(STAFFS.VERSION.eq((long) entity.getVersion()))
                .execute();
        return rows > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean restoreById(Staff entity) {
        int rows = dsl.update(STAFFS)
                .set(STAFFS.DELETED_AT, (LocalDateTime) null)
                .set(STAFFS.DELETED_BY, (Long) null)
                .where(STAFFS.ID.eq(entity.getId()))
                .and(STAFFS.DELETED_AT.isNotNull())
                .execute();
        return rows > 0;
    }

    /**
     * 検索条件から絞り込みクエリを組み立てます。
     *
     * @param condition 検索条件
     * @return 絞り込み済みクエリ
     */
    private SelectQuery<Record> applyFilters(StaffCondition condition) {
        SelectQuery<Record> q = dsl.selectQuery();
        q.addFrom(STAFFS);
        if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
            String kw = "%" + condition.getKeyword() + "%";
            q.addConditions(STAFFS.NAME.like(kw).or(STAFFS.EMAIL.like(kw)));
        }
        if (!condition.getRoles().isEmpty()) {
            q.addConditions(STAFFS.ROLE.in(condition.getRoles().stream().map(Long::valueOf).toList()));
        }
        return q;
    }

    /**
     * jOOQレコードをスタッフエンティティへ変換します。status は deletedAt の有無から算出します。
     *
     * @param rec jOOQレコード
     * @return スタッフエンティティ
     */
    private static Staff toEntity(Record rec) {
        Staff s = new Staff();
        s.setId(rec.get(STAFFS.ID));
        s.setName(rec.get(STAFFS.NAME));
        s.setEmail(rec.get(STAFFS.EMAIL));
        s.setProvider(Provider.from(rec.get(STAFFS.PROVIDER)));
        s.setProviderId(rec.get(STAFFS.PROVIDER_ID));
        s.setAvatar(rec.get(STAFFS.AVATAR));
        s.setRole(StaffRole.from(rec.get(STAFFS.ROLE).intValue()));
        s.setLastLoginAt(rec.get(STAFFS.LAST_LOGIN_AT));
        s.setCreatedAt(rec.get(STAFFS.CREATED_AT));
        s.setCreatedBy(rec.get(STAFFS.CREATED_BY));
        s.setUpdatedAt(rec.get(STAFFS.UPDATED_AT));
        s.setUpdatedBy(rec.get(STAFFS.UPDATED_BY));
        s.setDeletedAt(rec.get(STAFFS.DELETED_AT));
        s.setDeletedBy(rec.get(STAFFS.DELETED_BY));
        s.setVersion(rec.get(STAFFS.VERSION).intValue());
        s.setStatus(rec.get(STAFFS.DELETED_AT) == null ? StaffStatus.Active : StaffStatus.Inactive);
        return s;
    }
}
