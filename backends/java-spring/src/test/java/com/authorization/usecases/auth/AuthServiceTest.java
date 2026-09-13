/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.authorization.domain.staff.entities.Staff;
import com.authorization.domain.staff.enums.Provider;
import com.authorization.domain.staff.enums.StaffRole;
import com.authorization.domain.staff.mappers.SocialDtoMapperImpl;
import com.authorization.domain.staff.mappers.StaffApiMapperImpl;
import com.authorization.domain.staff.mappers.StaffConditionMapperImpl;
import com.authorization.domain.staff.valueobjects.StaffVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.fakes.FakeInvitationAuthRepository;
import com.authorization.support.fakes.FakeStaffRepository;
import com.authorization.usecases.auth.dtos.AuthUserDto;
import com.authorization.usecases.auth.dtos.SocialDto;
import org.junit.jupiter.api.Test;

/**
 * {@link AuthService} のユニットテストです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class AuthServiceTest {

    /**
     * 対象スタッフが存在しない場合、404（user_not_found）を投げることを確認します。
     */
    @Test
    void findUserThrowsNotFoundWhenStaffDoesNotExist() {
        AuthService service = newService(new FakeStaffRepository(), new FakeInvitationAuthRepository());

        AuthUserDto dto = new AuthUserDto();
        dto.setId(99L);
        AppException exception = assertThrows(AppException.class, () -> service.findUser(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * 既存スタッフでログインした場合、招待トークンを消費せずavatar/最終ログイン日時のみ更新することを確認します。
     */
    @Test
    void loginUpdatesAvatarAndLastLoginForExistingStaff() {
        Staff existing = makeStaff(1L, Provider.Google, "provider-1");
        FakeStaffRepository staffRepository = new FakeStaffRepository().add(existing);
        FakeInvitationAuthRepository invitationAuthRepository = new FakeInvitationAuthRepository();
        AuthService service = newService(staffRepository, invitationAuthRepository);

        SocialDto dto = new SocialDto();
        dto.setProvider(Provider.Google);
        dto.setProviderId("provider-1");
        dto.setAvatar("new-avatar");
        StaffVo vo = service.login(dto);

        assertEquals(1L, vo.getId());
        assertEquals("new-avatar", existing.getAvatar());
        assertNotNull(existing.getLastLoginAt());
        assertEquals(0, invitationAuthRepository.getRemovedTokens().size());
    }

    /**
     * 未登録スタッフが招待トークンなしでログインしようとした場合、403（invitation_required）を投げることを確認します。
     */
    @Test
    void loginThrowsForbiddenWhenNewStaffHasNoInvitationToken() {
        AuthService service = newService(new FakeStaffRepository(), new FakeInvitationAuthRepository());

        SocialDto dto = new SocialDto();
        dto.setProvider(Provider.Google);
        dto.setProviderId("unknown");
        AppException exception = assertThrows(AppException.class, () -> service.login(dto));

        assertEquals(403, exception.getStatusCode());
    }

    /**
     * 未登録スタッフが無効な招待トークンでログインしようとした場合、403（invitation_required）を投げることを確認します。
     */
    @Test
    void loginThrowsForbiddenWhenInvitationTokenIsInvalid() {
        AuthService service = newService(new FakeStaffRepository(), new FakeInvitationAuthRepository());

        SocialDto dto = new SocialDto();
        dto.setProvider(Provider.Google);
        dto.setProviderId("unknown");
        dto.setInvitationToken("invalid-token");
        AppException exception = assertThrows(AppException.class, () -> service.login(dto));

        assertEquals(403, exception.getStatusCode());
    }

    /**
     * 未登録スタッフが有効な招待トークンでログインした場合、トークンを消費して新規登録し、
     * 招待ロールをスタッフ権限へ設定することを確認します。
     */
    @Test
    void loginConsumesTokenAndRegistersNewStaffWithRole() {
        FakeInvitationAuthRepository invitationAuthRepository =
                new FakeInvitationAuthRepository().put("valid-token", StaffRole.Administrator.value());
        FakeStaffRepository staffRepository = new FakeStaffRepository();
        AuthService service = newService(staffRepository, invitationAuthRepository);

        SocialDto dto = new SocialDto();
        dto.setProvider(Provider.Google);
        dto.setProviderId("new-provider-id");
        dto.setName("New Staff");
        dto.setEmail("new@example.com");
        dto.setInvitationToken("valid-token");
        StaffVo vo = service.login(dto);

        assertNotNull(vo.getId());
        assertEquals(StaffRole.Administrator.value(), vo.getRole());
        assertFalse(invitationAuthRepository.contains("valid-token"));
        assertEquals(1, staffRepository.getPersistCallCount());
    }

    /**
     * テスト用のAuthServiceを組み立てます。
     *
     * @param staffRepository スタッフRepository（Fake）
     * @param invitationAuthRepository 招待認証Repository（Fake）
     * @return AuthService
     */
    private static AuthService newService(
            FakeStaffRepository staffRepository, FakeInvitationAuthRepository invitationAuthRepository) {
        return new AuthService(
                staffRepository,
                invitationAuthRepository,
                new StaffConditionMapperImpl(),
                new StaffApiMapperImpl(),
                new SocialDtoMapperImpl());
    }

    /**
     * テスト用のスタッフEntityを組み立てます。
     *
     * @param id スタッフID
     * @param provider プロバイダー
     * @param providerId プロバイダーID
     * @return スタッフEntity
     */
    private static Staff makeStaff(long id, Provider provider, String providerId) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setName("Staff " + id);
        staff.setEmail("staff" + id + "@example.com");
        staff.setProvider(provider);
        staff.setProviderId(providerId);
        staff.setRole(StaffRole.Member);
        staff.setVersion(1);
        return staff;
    }
}
