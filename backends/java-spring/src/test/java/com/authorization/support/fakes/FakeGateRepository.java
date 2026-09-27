/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.fakes;

import com.authorization.domain.gate.repositories.GateRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * テスト用の手書きFake認可（Gate）キャッシュRepositoryです（モックライブラリは使いません）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class FakeGateRepository implements GateRepository {

    private final Map<String, String> cache = new LinkedHashMap<>();
    private final List<String> callLog;

    /**
     * コンストラクタ。呼び出し順序の記録を必要としない場合はこちらを使います。
     */
    public FakeGateRepository() {
        this(new ArrayList<>());
    }

    /**
     * コンストラクタ。他のFakeと共有する呼び出し順序記録リストを受け取ります
     * （JWT履歴の保存との前後関係をテストするために使います）。
     *
     * @param callLog 呼び出し順序を記録する共有リスト
     */
    public FakeGateRepository(List<String> callLog) {
        this.callLog = callLog;
    }

    /**
     * キャッシュ済みJWTを事前に登録します。
     *
     * @param identifier クライアント識別名
     * @param memberId クライアント会員ID
     * @param token JWT文字列
     * @return このFake自身（メソッドチェーン用）
     */
    public FakeGateRepository seed(String identifier, String memberId, String token) {
        cache.put(cacheKey(identifier, memberId), token);
        return this;
    }

    /**
     * putJwt が呼ばれた回数を返します。
     *
     * @return 呼び出し回数
     */
    public int getPutJwtCallCount() {
        return (int) callLog.stream().filter("cache"::equals).count();
    }

    /** {@inheritDoc} */
    @Override
    public void putJwt(String identifier, String memberId, String token, int ttl) {
        callLog.add("cache");
        cache.put(cacheKey(identifier, memberId), token);
    }

    /** {@inheritDoc} */
    @Override
    public String getJwt(String identifier, String memberId) {
        return cache.get(cacheKey(identifier, memberId));
    }

    /**
     * キャッシュキーを組み立てます。
     *
     * @param identifier クライアント識別名
     * @param memberId クライアント会員ID
     * @return キャッシュキー
     */
    private static String cacheKey(String identifier, String memberId) {
        return identifier + ":" + memberId;
    }
}
