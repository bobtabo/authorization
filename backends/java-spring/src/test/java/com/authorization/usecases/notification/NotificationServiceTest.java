/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.authorization.domain.notification.entities.Notification;
import com.authorization.domain.notification.mappers.NotificationConditionMapperImpl;
import com.authorization.domain.notification.mappers.NotificationCreateMapperImpl;
import com.authorization.domain.staff.entities.Staff;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.fakes.FakeNotificationRepository;
import com.authorization.support.fakes.FakeStaffRepository;
import com.authorization.usecases.notification.dtos.NotificationCreateDto;
import com.authorization.usecases.notification.dtos.NotificationDto;
import org.junit.jupiter.api.Test;

/**
 * {@link NotificationService} のユニットテストです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class NotificationServiceTest {

    /**
     * 有効なスタッフ全員へ通知を配信し、各通知に配信先のstaffIdが設定されることを確認します。
     */
    @Test
    void fanOutCreatesNotificationForEachActiveStaff() {
        FakeStaffRepository staffRepository = new FakeStaffRepository().add(makeStaff(1L)).add(makeStaff(2L));
        FakeNotificationRepository notificationRepository = new FakeNotificationRepository();
        NotificationService service = newService(notificationRepository, staffRepository);

        NotificationCreateDto dto = new NotificationCreateDto();
        dto.setMessageType(1);
        dto.setTitle("Title");
        dto.setMessage("Message");
        dto.setExecutorId(9L);
        service.fanOut(dto);

        assertEquals(2, notificationRepository.getInsertedBatch().size());
        assertTrue(notificationRepository.getInsertedBatch().stream().anyMatch(n -> n.getStaffId() == 1L));
        assertTrue(notificationRepository.getInsertedBatch().stream().anyMatch(n -> n.getStaffId() == 2L));
    }

    /**
     * 通知が存在しない場合、既読更新は0件でも404を投げず、正常に0を返すことを確認します。
     */
    @Test
    void readsReturnsZeroWhenNoNotificationsMatch() {
        NotificationService service = newService(new FakeNotificationRepository(), new FakeStaffRepository());

        NotificationDto dto = new NotificationDto();
        dto.setStaffId(1L);
        dto.setAll(true);
        var vo = service.reads(dto);

        assertEquals(0, vo.getUpdated());
    }

    /**
     * 一括既読を実行すると、対象スタッフの未読通知が全て既読になることを確認します。
     */
    @Test
    void readsMarksAllUnreadNotificationsAsRead() {
        FakeNotificationRepository repository = new FakeNotificationRepository()
                .add(makeNotification(1L, 1L, false))
                .add(makeNotification(2L, 1L, false))
                .add(makeNotification(3L, 2L, false));
        NotificationService service = newService(repository, new FakeStaffRepository());

        NotificationDto dto = new NotificationDto();
        dto.setStaffId(1L);
        dto.setAll(true);
        var vo = service.reads(dto);

        assertEquals(2, vo.getUpdated());
    }

    /**
     * 対象通知が存在しない場合、404（notification_not_found）を投げることを確認します。
     */
    @Test
    void readThrowsNotFoundWhenNotificationDoesNotExist() {
        NotificationService service = newService(new FakeNotificationRepository(), new FakeStaffRepository());

        NotificationDto dto = new NotificationDto();
        dto.setStaffId(1L);
        dto.setNotificationId(99L);
        AppException exception = assertThrows(AppException.class, () -> service.read(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * 他スタッフの通知IDを指定した場合、所有者不一致のため404（notification_not_found）を投げることを確認します
     * （他スタッフの通知を既読にできてしまうことを防ぐ所有者チェック）。
     */
    @Test
    void readThrowsNotFoundWhenNotificationBelongsToAnotherStaff() {
        FakeNotificationRepository repository = new FakeNotificationRepository().add(makeNotification(1L, 2L, false));
        NotificationService service = newService(repository, new FakeStaffRepository());

        NotificationDto dto = new NotificationDto();
        dto.setStaffId(1L);
        dto.setNotificationId(1L);
        AppException exception = assertThrows(AppException.class, () -> service.read(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * 自分の通知を既読にした場合、成功結果を返すことを確認します。
     */
    @Test
    void readMarksOwnNotificationAsReadAndReturnsOk() {
        FakeNotificationRepository repository = new FakeNotificationRepository().add(makeNotification(1L, 1L, false));
        NotificationService service = newService(repository, new FakeStaffRepository());

        NotificationDto dto = new NotificationDto();
        dto.setStaffId(1L);
        dto.setNotificationId(1L);
        var vo = service.read(dto);

        assertTrue(vo.isOk());
    }

    /**
     * 通知件数の集計が、未読件数と総件数をそれぞれ正しく返すことを確認します。
     */
    @Test
    void countsReturnsUnreadAndTotalCounts() {
        FakeNotificationRepository repository = new FakeNotificationRepository()
                .add(makeNotification(1L, 1L, false))
                .add(makeNotification(2L, 1L, true));
        NotificationService service = newService(repository, new FakeStaffRepository());

        NotificationDto dto = new NotificationDto();
        dto.setStaffId(1L);
        var vo = service.counts(dto);

        assertEquals(2, vo.getTotal());
        assertEquals(1, vo.getUnread());
    }

    /**
     * テスト用のNotificationServiceを組み立てます。
     *
     * @param notificationRepository 通知Repository（Fake）
     * @param staffRepository スタッフRepository（Fake）
     * @return NotificationService
     */
    private static NotificationService newService(
            FakeNotificationRepository notificationRepository, FakeStaffRepository staffRepository) {
        return new NotificationService(
                notificationRepository,
                staffRepository,
                new NotificationConditionMapperImpl(),
                new NotificationCreateMapperImpl());
    }

    /**
     * テスト用のスタッフEntityを組み立てます。
     *
     * @param id スタッフID
     * @return スタッフEntity
     */
    private static Staff makeStaff(long id) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setName("Staff " + id);
        return staff;
    }

    /**
     * テスト用の通知Entityを組み立てます。
     *
     * @param id 通知ID
     * @param staffId 宛先スタッフID
     * @param read 既読フラグ
     * @return 通知Entity
     */
    private static Notification makeNotification(long id, long staffId, boolean read) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setStaffId(staffId);
        notification.setTitle("Title " + id);
        notification.setRead(read);
        return notification;
    }
}
