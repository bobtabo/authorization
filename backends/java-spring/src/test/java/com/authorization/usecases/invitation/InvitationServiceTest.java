/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.invitation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.authorization.domain.invitation.entities.Invitation;
import com.authorization.domain.invitation.valueobjects.InvitationVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.fakes.FakeInvitationAuthRepository;
import com.authorization.support.fakes.FakeInvitationRepository;
import com.authorization.usecases.invitation.dtos.InvitationDto;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * {@link InvitationService} のユニットテストです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class InvitationServiceTest {

    /**
     * 対象ロールの招待が存在しない場合、404（invitation_not_found）を投げることを確認します。
     */
    @Test
    void currentThrowsNotFoundWhenInvitationDoesNotExist() {
        InvitationService service = newService(new FakeInvitationRepository(), new FakeInvitationAuthRepository());

        InvitationDto dto = new InvitationDto();
        dto.setRole(1);
        AppException exception = assertThrows(AppException.class, () -> service.current(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * 対象ロールの招待が存在しない場合、issueも404（invitation_not_found）を投げることを確認します。
     */
    @Test
    void issueThrowsNotFoundWhenInvitationDoesNotExist() {
        InvitationService service = newService(new FakeInvitationRepository(), new FakeInvitationAuthRepository());

        InvitationDto dto = new InvitationDto();
        dto.setRole(1);
        AppException exception = assertThrows(AppException.class, () -> service.issue(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * 招待を発行すると、トークンが新しく生成され、旧トークンのキャッシュが無効化されることを確認します
     * （旧トークンのキャッシュが残ると、ローテーション後も旧トークンでログインできてしまうため）。
     */
    @Test
    void issueRotatesTokenAndInvalidatesOldTokenCache() {
        Invitation invitation = makeInvitation(1L, 2, "old-token");
        FakeInvitationRepository invitationRepository = new FakeInvitationRepository().add(invitation);
        FakeInvitationAuthRepository invitationAuthRepository =
                new FakeInvitationAuthRepository().put("old-token", 2);
        InvitationService service = newService(invitationRepository, invitationAuthRepository);

        InvitationDto dto = new InvitationDto();
        dto.setRole(2);
        dto.setExecutorId(9L);
        InvitationVo vo = service.issue(dto);

        assertNotEquals("old-token", vo.getToken());
        assertTrue(invitationAuthRepository.getRemovedTokens().contains("old-token"));
        assertFalse(invitationAuthRepository.contains("old-token"));
    }

    /**
     * 存在しないトークンの場合、400（invitation_invalid）を投げることを確認します。
     */
    @Test
    void findByTokenThrowsBadRequestWhenTokenDoesNotExist() {
        InvitationService service = newService(new FakeInvitationRepository(), new FakeInvitationAuthRepository());

        InvitationDto dto = new InvitationDto();
        dto.setToken("unknown");
        AppException exception = assertThrows(AppException.class, () -> service.findByToken(dto));

        assertEquals(400, exception.getStatusCode());
    }

    /**
     * トークンが空文字の場合、400（invitation_invalid）を投げることを確認します。
     */
    @Test
    void findByTokenThrowsBadRequestWhenTokenIsEmpty() {
        InvitationService service = newService(new FakeInvitationRepository(), new FakeInvitationAuthRepository());

        InvitationDto dto = new InvitationDto();
        dto.setToken("");
        AppException exception = assertThrows(AppException.class, () -> service.findByToken(dto));

        assertEquals(400, exception.getStatusCode());
    }

    /**
     * 有効なトークンの場合、ロールを認証キャッシュへ一時保存することを確認します。
     */
    @Test
    void findByTokenStoresRoleInAuthCache() {
        Invitation invitation = makeInvitation(1L, 1, "valid-token");
        FakeInvitationAuthRepository invitationAuthRepository = new FakeInvitationAuthRepository();
        InvitationService service =
                newService(new FakeInvitationRepository().add(invitation), invitationAuthRepository);

        InvitationDto dto = new InvitationDto();
        dto.setToken("valid-token");
        InvitationVo vo = service.findByToken(dto);

        assertTrue(vo.isFound());
        assertEquals(1, invitationAuthRepository.find("valid-token"));
    }

    /**
     * テスト用のInvitationServiceを組み立てます。
     *
     * @param invitationRepository 招待Repository（Fake）
     * @param invitationAuthRepository 招待認証Repository（Fake）
     * @return InvitationService
     */
    private static InvitationService newService(
            FakeInvitationRepository invitationRepository, FakeInvitationAuthRepository invitationAuthRepository) {
        return new InvitationService(invitationRepository, invitationAuthRepository, "https://example.com");
    }

    /**
     * テスト用の招待Entityを組み立てます。
     *
     * @param id 招待ID
     * @param role 権限
     * @param token 招待トークン
     * @return 招待Entity
     */
    private static Invitation makeInvitation(long id, int role, String token) {
        Invitation invitation = new Invitation();
        invitation.setId(id);
        invitation.setRole(role);
        invitation.setToken(token);
        invitation.setVersion(1);
        invitation.setCreatedAt(LocalDateTime.now());
        return invitation;
    }
}
