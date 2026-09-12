/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.infrastructure.persistence;

import static com.authorization.jooq.Tables.STAFFS;

import com.authorization.domain.staff.condition.StaffCondition;
import com.authorization.domain.staff.entities.Staff;
import com.authorization.domain.staff.mappers.StaffRecordMapper;
import com.authorization.domain.staff.repositories.StaffRepository;
import com.authorization.jooq.tables.records.StaffsRecord;
import com.authorization.support.exceptions.AppException;
import java.time.LocalDateTime;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortOrder;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

/**
 * jOOQ を使ったスタッフRepositoryの実装です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Component
public class JooqStaffRepository implements StaffRepository {

    private final DSLContext dsl;
    private final StaffRecordMapper recordMapper;

    /**
     * コンストラクタ。
     *
     * @param dsl jOOQ DSLContext
     * @param recordMapper スタッフ Entity/Record マッパー
     */
    public JooqStaffRepository(DSLContext dsl, StaffRecordMapper recordMapper) {
        this.dsl = dsl;
        this.recordMapper = recordMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countByCondition(StaffCondition condition) {
        return dsl.fetchCount(dsl.selectFrom(STAFFS).where(buildCondition(condition)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Staff> findByCondition(StaffCondition condition) {
        var step = dsl.selectFrom(STAFFS).where(buildCondition(condition));
        var option = condition.getOption();
        if (option != null) {
            String column = option.getOrderBy() != null ? option.getOrderBy() : option.getOrderByDesc();
            var sortField = switch (column == null ? "" : column) {
                case "name" -> STAFFS.NAME;
                case "role" -> STAFFS.ROLE;
                case "status" -> STAFFS.DELETED_AT;
                default -> STAFFS.CREATED_AT;
            };
            step.orderBy(sortField.sort(option.getOrderByDesc() != null ? SortOrder.DESC : SortOrder.ASC));
            if (condition.isPaging()) {
                step.limit(option.getOffset(), Math.clamp(option.getLimit(), 1, 500));
            }
        }
        return step.fetch().map(recordMapper::toEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Staff findById(StaffCondition condition) {
        StaffsRecord rec = dsl.selectFrom(STAFFS)
                .where(STAFFS.ID.eq(condition.getId()))
                .and(STAFFS.DELETED_AT.isNull())
                .fetchOne();
        return rec == null ? null : recordMapper.toEntity(rec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Staff findByProvider(StaffCondition condition) {
        StaffsRecord rec = dsl.selectFrom(STAFFS)
                .where(STAFFS.PROVIDER.eq(condition.getProvider().value()))
                .and(STAFFS.PROVIDER_ID.eq(condition.getProviderId()))
                .and(STAFFS.DELETED_AT.isNull())
                .fetchOne();
        return rec == null ? null : recordMapper.toEntity(rec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Staff> findAllActive() {
        return dsl.selectFrom(STAFFS).where(STAFFS.DELETED_AT.isNull()).fetch().map(recordMapper::toEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Staff persist(Staff entity) {
        if (entity.getId() == null) {
            StaffsRecord r = dsl.newRecord(STAFFS);
            recordMapper.fillRecord(entity, r);
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
     * 検索条件から絞り込み条件を組み立てます。
     *
     * @param condition 検索条件
     * @return 絞り込み条件
     */
    private static Condition buildCondition(StaffCondition condition) {
        Condition cond = DSL.noCondition();
        if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
            String kw = "%" + condition.getKeyword() + "%";
            cond = cond.and(STAFFS.NAME.like(kw).or(STAFFS.EMAIL.like(kw)));
        }
        if (!condition.getRoles().isEmpty()) {
            cond = cond.and(STAFFS.ROLE.in(condition.getRoles().stream().map(Long::valueOf).toList()));
        }
        return cond;
    }
}
