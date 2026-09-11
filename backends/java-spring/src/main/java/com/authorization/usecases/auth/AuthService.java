/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.auth;

import com.authorization.domain.invitation.repositories.InvitationAuthRepository;
import com.authorization.domain.staff.condition.StaffCondition;
import com.authorization.domain.staff.entities.Staff;
import com.authorization.domain.staff.enums.StaffRole;
import com.authorization.domain.staff.enums.StaffStatus;
import com.authorization.domain.staff.mappers.StaffApiMapper;
import com.authorization.domain.staff.mappers.StaffConditionMapper;
import com.authorization.domain.staff.repositories.StaffRepository;
import com.authorization.domain.staff.valueobjects.StaffVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.services.AbstractService;
import com.authorization.usecases.auth.dtos.AuthUserDto;
import com.authorization.usecases.auth.dtos.SocialDto;
import java.time.LocalDateTime;

/**
 * 認証Serviceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class AuthService extends AbstractService {

    private final StaffRepository staffRepository;
    private final InvitationAuthRepository invitationAuthRepository;
    private final StaffConditionMapper conditionMapper;
    private final StaffApiMapper apiMapper;

    /**
     * コンストラクタ。
     *
     * @param staffRepository スタッフRepository
     * @param invitationAuthRepository 招待認証Repository
     * @param conditionMapper DTO→Condition マッパー
     * @param apiMapper Entity→ValueObject マッパー
     */
    public AuthService(
            StaffRepository staffRepository,
            InvitationAuthRepository invitationAuthRepository,
            StaffConditionMapper conditionMapper,
            StaffApiMapper apiMapper) {
        this.staffRepository = staffRepository;
        this.invitationAuthRepository = invitationAuthRepository;
        this.conditionMapper = conditionMapper;
        this.apiMapper = apiMapper;
    }

    /**
     * ID でスタッフ（ログインユーザー）を取得します。
     *
     * @param dto 認証ユーザーDTO
     * @return スタッフValueObject
     */
    public StaffVo findUser(AuthUserDto dto) {
        StaffCondition condition = new StaffCondition();
        condition.setId(dto.getId());

        Staff entity = staffRepository.findById(condition);
        if (entity == null) {
            throw AppException.notFound("user_not_found");
        }

        return apiMapper.toVo(entity);
    }

    /**
     * ソーシャル認証でログインします（未登録の場合は新規作成します）。
     *
     * @param dto ソーシャルDTO
     * @return スタッフValueObject
     */
    public StaffVo login(SocialDto dto) {
        StaffCondition condition = conditionMapper.toCondition(dto);
        Staff entity = staffRepository.findByProvider(condition);

        Staff saved;
        if (entity == null) {
            String token = dto.getInvitationToken();
            Integer roleValue = (token == null || token.isEmpty()) ? null : invitationAuthRepository.find(token);
            if (roleValue == null) {
                throw AppException.forbidden("invitation_required");
            }
            invitationAuthRepository.remove(token);

            Staff newEntity = new Staff();
            newEntity.setName(dto.getName());
            newEntity.setEmail(dto.getEmail());
            newEntity.setProvider(dto.getProvider());
            newEntity.setProviderId(dto.getProviderId());
            newEntity.setAvatar(dto.getAvatar());
            newEntity.setRole(StaffRole.from(roleValue));
            newEntity.setStatus(StaffStatus.Active);
            newEntity.setLastLoginAt(LocalDateTime.now());
            newEntity.assignCreated(0);
            saved = staffRepository.persist(newEntity);
        } else {
            entity.setAvatar(dto.getAvatar());
            entity.setLastLoginAt(LocalDateTime.now());
            entity.assignUpdated(entity.getId());
            saved = staffRepository.persist(entity);
        }

        return apiMapper.toVo(saved);
    }
}
