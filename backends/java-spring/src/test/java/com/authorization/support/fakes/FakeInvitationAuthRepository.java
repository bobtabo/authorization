/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.fakes;

import com.authorization.domain.invitation.repositories.InvitationAuthRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * テスト用の手書きFake招待認証Repositoryです（モックライブラリは使いません）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class FakeInvitationAuthRepository implements InvitationAuthRepository {

    private final Map<String, Integer> tokenToRole = new LinkedHashMap<>();
    private final List<String> removedTokens = new ArrayList<>();

    /**
     * トークンとロールを事前に登録します。
     *
     * @param token 招待トークン
     * @param role 権限
     * @return このFake自身（メソッドチェーン用）
     */
    public FakeInvitationAuthRepository put(String token, int role) {
        tokenToRole.put(token, role);
        return this;
    }

    /**
     * remove が呼ばれたトークンの一覧を返します。
     *
     * @return 削除されたトークンの一覧
     */
    public List<String> getRemovedTokens() {
        return removedTokens;
    }

    /**
     * 指定トークンが登録されているか確認します。
     *
     * @param token 招待トークン
     * @return 登録されている場合 true
     */
    public boolean contains(String token) {
        return tokenToRole.containsKey(token);
    }

    /** {@inheritDoc} */
    @Override
    public void store(String token, int role, int ttl) {
        tokenToRole.put(token, role);
    }

    /** {@inheritDoc} */
    @Override
    public Integer find(String token) {
        return tokenToRole.get(token);
    }

    /** {@inheritDoc} */
    @Override
    public void remove(String token) {
        tokenToRole.remove(token);
        removedTokens.add(token);
    }
}
