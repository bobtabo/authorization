/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.staff;

import com.authorization.domain.staff.condition.StaffCondition;
import com.authorization.domain.staff.entities.Staff;
import com.authorization.domain.staff.mappers.StaffApiMapper;
import com.authorization.domain.staff.mappers.StaffConditionMapper;
import com.authorization.domain.staff.repositories.StaffRepository;
import com.authorization.domain.staff.valueobjects.StaffListVo;
import com.authorization.domain.staff.valueobjects.StaffMutationVo;
import com.authorization.domain.staff.valueobjects.StaffRemoveVo;
import com.authorization.domain.staff.valueobjects.StaffResourceVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.repositories.conditions.Option;
import com.authorization.support.services.AbstractService;
import com.authorization.usecases.staff.dtos.StaffDto;
import java.util.List;

/**
 * スタッフの取得・一覧・更新・削除のユースケースをまとめるServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class StaffService extends AbstractService {

    private final StaffRepository repository;
    private final StaffConditionMapper conditionMapper;
    private final StaffApiMapper apiMapper;

    /**
     * コンストラクタ。
     *
     * @param repository スタッフRepository
     * @param conditionMapper DTO→Condition マッパー
     * @param apiMapper Entity→ValueObject マッパー
     */
    public StaffService(StaffRepository repository, StaffConditionMapper conditionMapper, StaffApiMapper apiMapper) {
        this.repository = repository;
        this.conditionMapper = conditionMapper;
        this.apiMapper = apiMapper;
    }

    /**
     * ID でスタッフを1件取得します。
     *
     * @param dto スタッフDTO
     * @return スタッフリソースValueObject
     */
    public StaffResourceVo find(StaffDto dto) {
        StaffResourceVo vo = new StaffResourceVo();
        if (dto.getId() == null) {
            return vo;
        }

        StaffCondition condition = new StaffCondition();
        condition.setId(dto.getId());

        Staff entity = repository.findById(condition);
        if (entity == null) {
            return vo;
        }

        return apiMapper.toResourceVo(entity);
    }

    /**
     * 条件でスタッフ一覧を取得します。
     *
     * @param dto スタッフDTO
     * @return スタッフ一覧ValueObject
     */
    public StaffListVo index(StaffDto dto) {
        StaffCondition condition = conditionMapper.toCondition(dto);
        condition.setOption(new Option(dto.getOffset(), dto.getLimit(), dto.getSort(), dto.getSortType()));

        int count = repository.countByCondition(condition);
        List<Staff> list = repository.findByCondition(condition);

        StaffListVo vo = new StaffListVo();
        vo.setItems(list.stream().map(apiMapper::toResourceVo).toList());
        vo.setCount(count);
        vo.setOffset(dto.getOffset());
        vo.setLimit(dto.getLimit());
        vo.setSort(dto.getSort());
        vo.setSortType(dto.getSortType());
        return vo;
    }

    /**
     * 権限を更新します。
     *
     * @param dto スタッフDTO
     * @return スタッフ権限更新ValueObject
     */
    public StaffMutationVo updateRole(StaffDto dto) {
        if (dto.getRole() == null) {
            throw AppException.badRequest("role_invalid");
        }

        StaffCondition condition = conditionMapper.toCondition(dto);
        Staff entity = repository.findById(condition);
        if (entity == null) {
            throw AppException.notFound("staff_not_found");
        }

        entity.setRole(dto.getRole());
        entity.assignUpdated(dto.getExecutorId());
        Staff saved = repository.persist(entity);

        StaffMutationVo vo = new StaffMutationVo();
        vo.setOk(true);
        vo.setId(saved.getId());
        return vo;
    }

    /**
     * スタッフを論理削除します。
     *
     * @param dto スタッフDTO
     * @return スタッフ削除ValueObject
     */
    public StaffRemoveVo destroy(StaffDto dto) {
        Staff entity = new Staff();
        entity.setId(dto.getId());
        entity.setVersion(dto.getVersion());
        entity.assignDeleted(dto.getExecutorId());

        boolean result = repository.deleteById(entity);
        if (!result) {
            throw AppException.notFound("staff_not_found");
        }

        StaffRemoveVo vo = new StaffRemoveVo();
        vo.setOk(true);
        vo.setId(dto.getId());
        return vo;
    }

    /**
     * スタッフの論理削除を復元します。
     *
     * @param dto スタッフDTO
     * @return スタッフ復元ValueObject
     */
    public StaffRemoveVo restore(StaffDto dto) {
        Staff entity = new Staff();
        entity.setId(dto.getId());

        boolean result = repository.restoreById(entity);
        if (!result) {
            throw AppException.notFound("staff_not_found");
        }

        StaffRemoveVo vo = new StaffRemoveVo();
        vo.setOk(true);
        vo.setId(dto.getId());
        return vo;
    }
}
