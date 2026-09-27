/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.fakes;

import com.authorization.domain.client.condition.ClientCondition;
import com.authorization.domain.client.entities.Client;
import com.authorization.domain.client.enums.ClientStatus;
import com.authorization.domain.client.repositories.ClientRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * テスト用の手書きFakeクライアントRepositoryです（モックライブラリは使いません）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class FakeClientRepository implements ClientRepository {

    private final Map<Long, Client> clients = new LinkedHashMap<>();
    private long nextId = 1;
    private int persistCallCount;
    private int deleteByIdCallCount;

    /**
     * クライアントを追加します。id未設定の場合は自動採番します。
     *
     * @param client 追加するクライアントEntity
     * @return このFake自身（メソッドチェーン用）
     */
    public FakeClientRepository add(Client client) {
        if (client.getId() == null) {
            client.setId(nextId++);
        }
        clients.put(client.getId(), client);
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

    /**
     * deleteById が呼ばれた回数を返します。
     *
     * @return 呼び出し回数
     */
    public int getDeleteByIdCallCount() {
        return deleteByIdCallCount;
    }

    /** {@inheritDoc} */
    @Override
    public List<Client> findByCondition(ClientCondition condition) {
        return new ArrayList<>(clients.values());
    }

    /** {@inheritDoc} */
    @Override
    public int countByCondition(ClientCondition condition) {
        return clients.size();
    }

    /**
     * {@inheritDoc}
     * 実装（jOOQ）と同様に、削除済みかどうかにかかわらず取得します
     * （アーカイブ表示のため、PHP版の {@code withTrashed()} と同じ挙動）。
     */
    @Override
    public Client findById(ClientCondition condition) {
        return clients.get(condition.getId());
    }

    /** {@inheritDoc} */
    @Override
    public Client persist(Client entity) {
        persistCallCount++;
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        clients.put(entity.getId(), entity);
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public Client findByAccessToken(ClientCondition condition) {
        return clients.values().stream()
                .filter(client -> client.getDeletedAt() == null)
                .filter(client -> client.getStatus() == ClientStatus.Active)
                .filter(client -> Objects.equals(client.getAccessToken(), condition.getAccessToken()))
                .findFirst()
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public Client findByIdentifier(ClientCondition condition) {
        return clients.values().stream()
                .filter(client -> client.getDeletedAt() == null)
                .filter(client -> Objects.equals(client.getIdentifier(), condition.getIdentifier()))
                .findFirst()
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteById(Client entity) {
        deleteByIdCallCount++;
        Client existing = clients.get(entity.getId());
        if (existing == null || !Objects.equals(existing.getVersion(), entity.getVersion())) {
            return false;
        }
        existing.setDeletedAt(entity.getDeletedAt());
        existing.setDeletedBy(entity.getDeletedBy());
        return true;
    }
}
