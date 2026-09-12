/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.invitation;

import com.authorization.domain.invitation.condition.InvitationCondition;
import com.authorization.domain.invitation.entities.Invitation;
import com.authorization.domain.invitation.repositories.InvitationAuthRepository;
import com.authorization.domain.invitation.repositories.InvitationRepository;
import com.authorization.domain.invitation.valueobjects.InvitationVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.services.AbstractService;
import com.authorization.usecases.invitation.dtos.InvitationDto;
import java.security.SecureRandom;

/**
 * 招待Serviceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class InvitationService extends AbstractService {

    private final InvitationRepository invitationRepository;
    private final InvitationAuthRepository invitationAuthRepository;
    private final String frontendUrl;

    /**
     * コンストラクタ。
     *
     * @param invitationRepository 招待Repository
     * @param invitationAuthRepository 招待認証Repository
     * @param frontendUrl フロントエンドURL
     */
    public InvitationService(
            InvitationRepository invitationRepository,
            InvitationAuthRepository invitationAuthRepository,
            String frontendUrl) {
        this.invitationRepository = invitationRepository;
        this.invitationAuthRepository = invitationAuthRepository;
        this.frontendUrl = frontendUrl;
    }

    /**
     * 現在の招待情報を取得します。
     *
     * @param dto 招待DTO
     * @return 招待ValueObject
     */
    public InvitationVo current(InvitationDto dto) {
        Invitation entity = invitationRepository.getCurrentByRole(dto.getRole() != null ? dto.getRole() : 2);
        if (entity == null) {
            throw AppException.notFound("invitation_not_found");
        }
        return toVo(entity.getToken());
    }

    /**
     * 新しい招待を発行します（既存の招待のトークンを再生成します。新規行は作成しません）。
     *
     * @param dto 招待DTO
     * @return 招待ValueObject
     */
    public InvitationVo issue(InvitationDto dto) {
        Invitation entity = invitationRepository.getCurrentByRole(dto.getRole() != null ? dto.getRole() : 2);
        if (entity == null) {
            throw AppException.notFound("invitation_not_found");
        }
        String oldToken = entity.getToken();
        entity.setToken(generateHex(16));
        entity.assignUpdated(dto.getExecutorId() == null ? 0 : dto.getExecutorId());
        Invitation saved = invitationRepository.persist(entity);
        invitationAuthRepository.remove(oldToken);

        return toVo(saved.getToken());
    }

    /**
     * 招待トークンから招待情報を解決します。
     *
     * @param dto 招待DTO
     * @return 招待ValueObject
     */
    public InvitationVo findByToken(InvitationDto dto) {
        String token = dto.getToken();
        if (token == null || token.isEmpty()) {
            throw AppException.badRequest("invitation_invalid");
        }

        InvitationCondition condition = new InvitationCondition();
        condition.setToken(token);
        Invitation entity = invitationRepository.findByToken(condition);
        if (entity == null) {
            throw AppException.badRequest("invitation_invalid");
        }

        // 招待トークンとロールを一時保存（10分間）
        invitationAuthRepository.store(entity.getToken(), entity.getRole() != null ? entity.getRole() : 2, 600);

        return toVo(entity.getToken());
    }

    /**
     * トークンから招待 ValueObject を組み立てます。
     *
     * @param token 招待トークン
     * @return 招待ValueObject
     */
    private InvitationVo toVo(String token) {
        String url = buildUrl(token);
        InvitationVo vo = new InvitationVo();
        vo.setFound(true);
        vo.setUrl(url);
        vo.setDisplayUrl(buildDisplayUrl(url));
        vo.setToken(token);
        return vo;
    }

    /**
     * トークンから完全な招待 URL を生成します。
     *
     * @param token 招待トークン
     * @return 完全 URL
     */
    private String buildUrl(String token) {
        String base = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        return base + "/invitation/" + token;
    }

    /**
     * 表示用に {@code /invitation/} 以降のトークンを省略した URL を返します。
     *
     * @param url 完全 URL
     * @return 省略表示用 URL
     */
    private static String buildDisplayUrl(String url) {
        return buildDisplayUrl(url, 6, 4);
    }

    /**
     * 表示用に {@code /invitation/} 以降のトークンを省略した URL を返します。
     *
     * @param url 完全 URL
     * @param head トークン先頭から表示する文字数
     * @param tail トークン末尾から表示する文字数
     * @return 省略表示用 URL
     */
    private static String buildDisplayUrl(String url, int head, int tail) {
        String segment = "/invitation/";
        int idx = url.indexOf(segment);
        if (idx < 0) {
            return url.length() > 72 ? url.substring(0, 68) + "..." : url;
        }

        String base = url.substring(0, idx + segment.length());
        String after = url.substring(idx + segment.length());

        int suffixStart = 0;
        while (suffixStart < after.length() && after.charAt(suffixStart) != '?' && after.charAt(suffixStart) != '#') {
            suffixStart++;
        }
        String token = after.substring(0, suffixStart);
        String suffix = after.substring(suffixStart);

        if (token.length() <= head + tail + 3) {
            return url;
        }

        return base + token.substring(0, head) + "..." + token.substring(token.length() - tail) + suffix;
    }

    /**
     * 暗号論的乱数から 16 進文字列を生成します。
     *
     * @param byteCount 生成するランダムバイト数
     * @return 小文字16進文字列
     */
    private static String generateHex(int byteCount) {
        byte[] buf = new byte[byteCount];
        new SecureRandom().nextBytes(buf);
        StringBuilder sb = new StringBuilder(byteCount * 2);
        for (byte b : buf) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
