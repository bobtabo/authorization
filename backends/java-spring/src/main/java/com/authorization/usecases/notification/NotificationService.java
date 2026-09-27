/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.notification;

import com.authorization.domain.notification.condition.NotificationCondition;
import com.authorization.domain.notification.entities.Notification;
import com.authorization.domain.notification.mappers.NotificationConditionMapper;
import com.authorization.domain.notification.mappers.NotificationCreateMapper;
import com.authorization.domain.notification.repositories.NotificationRepository;
import com.authorization.domain.notification.valueobjects.NotificationCountsVo;
import com.authorization.domain.notification.valueobjects.NotificationListVo;
import com.authorization.domain.notification.valueobjects.NotificationSaveVo;
import com.authorization.domain.staff.entities.Staff;
import com.authorization.domain.staff.repositories.StaffRepository;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.services.AbstractService;
import com.authorization.usecases.notification.dtos.NotificationCreateDto;
import com.authorization.usecases.notification.dtos.NotificationDto;
import java.util.List;

/**
 * 通知Serviceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class NotificationService extends AbstractService {

    private final NotificationRepository notificationRepository;
    private final StaffRepository staffRepository;
    private final NotificationConditionMapper conditionMapper;
    private final NotificationCreateMapper createMapper;

    /**
     * コンストラクタ。
     *
     * @param notificationRepository 通知Repository
     * @param staffRepository スタッフRepository
     * @param conditionMapper DTO→Condition マッパー
     * @param createMapper DTO→Entity マッパー
     */
    public NotificationService(
            NotificationRepository notificationRepository,
            StaffRepository staffRepository,
            NotificationConditionMapper conditionMapper,
            NotificationCreateMapper createMapper) {
        this.notificationRepository = notificationRepository;
        this.staffRepository = staffRepository;
        this.conditionMapper = conditionMapper;
        this.createMapper = createMapper;
    }

    /**
     * カーソル付きで通知一覧ページを取得します。
     *
     * @param dto 通知DTO
     * @return 通知一覧ValueObject
     */
    public NotificationListVo listPage(NotificationDto dto) {
        NotificationCondition condition = conditionMapper.toCondition(dto);
        condition.setLimit(Math.max(1, Math.min(100, dto.getLimit())));

        List<Notification> list = notificationRepository.listPage(condition);
        boolean hasNext = list.size() > condition.getLimit();
        List<Notification> items = hasNext ? list.subList(0, condition.getLimit()) : list;

        String nextCursor = null;
        if (hasNext) {
            Notification last = items.get(items.size() - 1);
            String raw = last.getCreatedAt() + "," + last.getId();
            nextCursor = java.util.Base64.getEncoder().encodeToString(raw.getBytes());
        }

        NotificationListVo vo = new NotificationListVo();
        vo.setItems(items);
        vo.setNextCursor(nextCursor);
        return vo;
    }

    /**
     * 通知件数の集計を取得します。
     *
     * @param dto 通知DTO
     * @return 通知件数ValueObject
     */
    public NotificationCountsVo counts(NotificationDto dto) {
        NotificationCondition condition = conditionMapper.toCondition(dto);
        int total = notificationRepository.counts(condition);

        condition.setCountUnread(true);
        int unread = notificationRepository.counts(condition);

        NotificationCountsVo vo = new NotificationCountsVo();
        vo.setTotal(total);
        vo.setUnread(unread);
        return vo;
    }

    /**
     * 一括既読などの更新を行います。
     *
     * @param dto 通知DTO
     * @return 通知更新ValueObject
     */
    public NotificationSaveVo reads(NotificationDto dto) {
        NotificationCondition condition = conditionMapper.toCondition(dto);
        int updated = notificationRepository.updateRead(condition);

        NotificationSaveVo vo = new NotificationSaveVo();
        vo.setUpdated(updated);
        return vo;
    }

    /**
     * 有効なスタッフ全員へ通知を配信します（ファンアウト）。
     *
     * @param dto 通知登録DTO
     */
    public void fanOut(NotificationCreateDto dto) {
        List<Staff> staffs = staffRepository.findAllActive();

        List<Notification> notifications = staffs.stream().map(staff -> {
            Notification entity = createMapper.toEntity(dto);
            entity.setStaffId(staff.getId());
            entity.assignCreated(dto.getExecutorId());
            return entity;
        }).toList();

        notificationRepository.insertBatch(notifications);
    }

    /**
     * 単一通知を部分更新します。
     *
     * @param dto 通知DTO
     * @return 通知更新ValueObject
     */
    public NotificationSaveVo read(NotificationDto dto) {
        NotificationCondition condition = conditionMapper.toCondition(dto);
        condition.setId(dto.getNotificationId());

        int result = notificationRepository.updateRead(condition);
        if (result == 0) {
            throw AppException.notFound("notification_not_found");
        }

        NotificationSaveVo vo = new NotificationSaveVo();
        vo.setOk(true);
        vo.setId(dto.getNotificationId());
        return vo;
    }
}
