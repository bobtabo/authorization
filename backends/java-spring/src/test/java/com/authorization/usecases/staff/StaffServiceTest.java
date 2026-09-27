/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.staff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.authorization.domain.staff.condition.StaffCondition;
import com.authorization.domain.staff.entities.Staff;
import com.authorization.domain.staff.enums.StaffRole;
import com.authorization.domain.staff.mappers.StaffApiMapperImpl;
import com.authorization.domain.staff.mappers.StaffConditionMapperImpl;
import com.authorization.domain.staff.valueobjects.StaffListVo;
import com.authorization.domain.staff.valueobjects.StaffMutationVo;
import com.authorization.domain.staff.valueobjects.StaffRemoveVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.fakes.FakeStaffRepository;
import com.authorization.usecases.staff.dtos.StaffDto;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * {@link StaffService} のユニットテストです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class StaffServiceTest {

    /**
     * 指定IDのスタッフが存在しない場合、found が false のValueObjectを返すことを確認します。
     */
    @Test
    void findReturnsNotFoundWhenStaffDoesNotExist() {
        StaffService service = newService(new FakeStaffRepository());

        var vo = service.find(dto(99L));

        assertFalse(vo.isFound());
    }

    /**
     * 一覧取得が、Repositoryから取得した件数とアイテムをそのままValueObjectへ反映することを確認します。
     */
    @Test
    void indexReturnsItemsAndCountFromRepository() {
        FakeStaffRepository repository = new FakeStaffRepository()
                .add(makeStaff(1L, StaffRole.Member))
                .add(makeStaff(2L, StaffRole.Administrator));
        StaffService service = newService(repository);

        StaffDto dto = new StaffDto();
        dto.setLimit(10);
        dto.setPaging(1);
        StaffListVo vo = service.index(dto);

        assertEquals(2, vo.getCount());
        assertEquals(2, vo.getItems().size());
    }

    /**
     * 権限指定が無い場合、400（role_invalid）を投げることを確認します。
     */
    @Test
    void updateRoleThrowsBadRequestWhenRoleIsMissing() {
        StaffService service = newService(new FakeStaffRepository().add(makeStaff(1L, StaffRole.Member)));

        StaffDto dto = dto(1L);
        AppException exception = assertThrows(AppException.class, () -> service.updateRole(dto));

        assertEquals(400, exception.getStatusCode());
    }

    /**
     * 対象スタッフが存在しない場合、404（staff_not_found）を投げることを確認します。
     */
    @Test
    void updateRoleThrowsNotFoundWhenStaffDoesNotExist() {
        StaffService service = newService(new FakeStaffRepository().add(makeStaff(9L, StaffRole.Administrator)));

        StaffDto dto = dto(99L);
        dto.setRole(StaffRole.Administrator);
        AppException exception = assertThrows(AppException.class, () -> service.updateRole(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * 正しい入力の場合、Repositoryへ反映され成功結果を返すことを確認します。
     */
    @Test
    void updateRoleUpdatesRoleAndReturnsOk() {
        FakeStaffRepository repository = new FakeStaffRepository()
                .add(makeStaff(1L, StaffRole.Member))
                .add(makeStaff(9L, StaffRole.Administrator));
        StaffService service = newService(repository);

        StaffDto dto = dto(1L);
        dto.setRole(StaffRole.Administrator);
        StaffMutationVo vo = service.updateRole(dto);

        assertTrue(vo.isOk());
        assertEquals(1, repository.getPersistCallCount());
        StaffCondition condition = new StaffCondition();
        condition.setId(1L);
        assertEquals(StaffRole.Administrator, repository.findById(condition).getRole());
    }

    /**
     * 実行者が未認証（executorId未設定）の場合、401（unauthenticated）を投げることを確認します。
     */
    @Test
    void updateRoleThrowsUnauthorizedWhenExecutorIdIsMissing() {
        StaffService service = newService(new FakeStaffRepository().add(makeStaff(1L, StaffRole.Member)));

        StaffDto dto = dto(1L);
        dto.setExecutorId(0L);
        dto.setRole(StaffRole.Administrator);
        AppException exception = assertThrows(AppException.class, () -> service.updateRole(dto));

        assertEquals(401, exception.getStatusCode());
    }

    /**
     * 実行者がAdmin以外の場合、403（forbidden）を投げることを確認します。
     */
    @Test
    void updateRoleThrowsForbiddenWhenExecutorIsNotAdmin() {
        FakeStaffRepository repository = new FakeStaffRepository()
                .add(makeStaff(1L, StaffRole.Member))
                .add(makeStaff(9L, StaffRole.Member));
        StaffService service = newService(repository);

        StaffDto dto = dto(1L);
        dto.setRole(StaffRole.Administrator);
        AppException exception = assertThrows(AppException.class, () -> service.updateRole(dto));

        assertEquals(403, exception.getStatusCode());
    }

    /**
     * 実行者が無効化（論理削除）済みAdminの場合、403（forbidden）を投げることを確認します。
     * 署名済みクッキーは有効だが、実行者は既に無効化されている状態を再現します。
     */
    @Test
    void updateRoleThrowsForbiddenWhenExecutorIsDeletedAdmin() {
        Staff deletedAdmin = makeStaff(9L, StaffRole.Administrator);
        deletedAdmin.setDeletedAt(LocalDateTime.now());
        FakeStaffRepository repository = new FakeStaffRepository()
                .add(makeStaff(1L, StaffRole.Member))
                .add(deletedAdmin);
        StaffService service = newService(repository);

        StaffDto dto = dto(1L);
        dto.setRole(StaffRole.Administrator);
        AppException exception = assertThrows(AppException.class, () -> service.updateRole(dto));

        assertEquals(403, exception.getStatusCode());
    }

    /**
     * 対象スタッフが存在しない場合、404（staff_not_found）を投げることを確認します。
     */
    @Test
    void destroyThrowsNotFoundWhenStaffDoesNotExist() {
        StaffService service = newService(new FakeStaffRepository());

        StaffDto dto = dto(99L);
        AppException exception = assertThrows(AppException.class, () -> service.destroy(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * バージョンが一致しない場合、Repository側の楽観ロック判定により404（staff_not_found）を投げることを確認します。
     */
    @Test
    void destroyThrowsNotFoundWhenVersionIsStale() {
        Staff staff = makeStaff(1L, StaffRole.Member);
        staff.setVersion(3);
        StaffService service = newService(new FakeStaffRepository().add(staff));

        StaffDto dto = dto(1L);
        dto.setVersion(1);
        AppException exception = assertThrows(AppException.class, () -> service.destroy(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * 正しい入力の場合、論理削除して成功結果を返すことを確認します。
     */
    @Test
    void destroySoftDeletesAndReturnsOk() {
        Staff staff = makeStaff(1L, StaffRole.Member);
        staff.setVersion(1);
        FakeStaffRepository repository = new FakeStaffRepository().add(staff);
        StaffService service = newService(repository);

        StaffDto dto = dto(1L);
        dto.setVersion(1);
        StaffRemoveVo vo = service.destroy(dto);

        assertTrue(vo.isOk());
        assertEquals(1, repository.getDeleteByIdCallCount());
    }

    /**
     * 削除されていないスタッフを復元しようとした場合、404（staff_not_found）を投げることを確認します。
     */
    @Test
    void restoreThrowsNotFoundWhenStaffIsNotDeleted() {
        StaffService service = newService(new FakeStaffRepository().add(makeStaff(1L, StaffRole.Member)));

        StaffDto dto = dto(1L);
        AppException exception = assertThrows(AppException.class, () -> service.restore(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * 削除済みスタッフを復元した場合、成功結果を返すことを確認します。
     */
    @Test
    void restoreRestoresDeletedStaffAndReturnsOk() {
        Staff staff = makeStaff(1L, StaffRole.Member);
        staff.setDeletedAt(LocalDateTime.now());
        FakeStaffRepository repository = new FakeStaffRepository().add(staff);
        StaffService service = newService(repository);

        StaffRemoveVo vo = service.restore(dto(1L));

        assertTrue(vo.isOk());
        assertEquals(1, repository.getRestoreByIdCallCount());
    }

    /**
     * テスト用のStaffServiceを組み立てます。
     *
     * @param repository スタッフRepository（Fake）
     * @return StaffService
     */
    private static StaffService newService(FakeStaffRepository repository) {
        return new StaffService(repository, new StaffConditionMapperImpl(), new StaffApiMapperImpl());
    }

    /**
     * テスト用のスタッフDTOを組み立てます。
     *
     * @param id スタッフID
     * @return スタッフDTO
     */
    private static StaffDto dto(long id) {
        StaffDto dto = new StaffDto();
        dto.setId(id);
        dto.setExecutorId(9L);
        return dto;
    }

    /**
     * テスト用のスタッフEntityを組み立てます。
     *
     * @param id スタッフID
     * @param role 権限
     * @return スタッフEntity
     */
    private static Staff makeStaff(long id, StaffRole role) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setName("Staff " + id);
        staff.setEmail("staff" + id + "@example.com");
        staff.setRole(role);
        staff.setVersion(1);
        return staff;
    }
}
