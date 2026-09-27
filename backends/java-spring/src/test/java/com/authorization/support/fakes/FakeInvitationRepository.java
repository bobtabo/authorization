/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.fakes;

import com.authorization.domain.invitation.condition.InvitationCondition;
import com.authorization.domain.invitation.entities.Invitation;
import com.authorization.domain.invitation.repositories.InvitationRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * テスト用の手書きFake招待Repositoryです（モックライブラリは使いません）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class FakeInvitationRepository implements InvitationRepository {

    private final Map<Long, Invitation> invitations = new LinkedHashMap<>();
    private long nextId = 1;
    private int persistCallCount;

    /**
     * 招待を追加します。id未設定の場合は自動採番します。
     *
     * @param invitation 追加する招待Entity
     * @return このFake自身（メソッドチェーン用）
     */
    public FakeInvitationRepository add(Invitation invitation) {
        if (invitation.getId() == null) {
            invitation.setId(nextId++);
        }
        invitations.put(invitation.getId(), invitation);
        return this;
    }

    /**
     * persist が呼ばれた回数を返します。
     *
     * @return 呼び出し回数
     */
    public int getPersistCallCount() {
        return persistCallCount;
    }

    /** {@inheritDoc} */
    @Override
    public Invitation getCurrentByRole(int role) {
        return invitations.values().stream()
                .filter(invitation -> invitation.getDeletedAt() == null)
                .filter(invitation -> invitation.getRole() != null && invitation.getRole() == role)
                .max(Comparator.comparing(Invitation::getCreatedAt))
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public Invitation persist(Invitation entity) {
        persistCallCount++;
        invitations.put(entity.getId(), entity);
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public Invitation findByToken(InvitationCondition condition) {
        return invitations.values().stream()
                .filter(invitation -> invitation.getDeletedAt() == null)
                .filter(invitation -> Objects.equals(invitation.getToken(), condition.getToken()))
                .findFirst()
                .orElse(null);
    }
}
