/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.fakes;

import com.authorization.domain.client.condition.JwtHistoryCondition;
import com.authorization.domain.client.entities.JwtHistory;
import com.authorization.domain.client.repositories.JwtHistoryRepository;
import com.authorization.support.repositories.conditions.Option;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * テスト用の手書きFakeJWT履歴Repositoryです（モックライブラリは使いません）。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class FakeJwtHistoryRepository implements JwtHistoryRepository {

    private final List<JwtHistory> histories = new ArrayList<>();
    private final List<String> callLog;
    private long nextId = 1;
    private int persistCallCount;

    /**
     * コンストラクタ。呼び出し順序の記録を必要としない場合はこちらを使います。
     */
    public FakeJwtHistoryRepository() {
        this(new ArrayList<>());
    }

    /**
     * コンストラクタ。他のFakeと共有する呼び出し順序記録リストを受け取ります
     * （キャッシュ書き込みとの前後関係をテストするために使います）。
     *
     * @param callLog 呼び出し順序を記録する共有リスト
     */
    public FakeJwtHistoryRepository(List<String> callLog) {
        this.callLog = callLog;
    }

    /**
     * JWT履歴を追加します。id未設定の場合は自動採番します。
     *
     * @param history 追加するJWT履歴Entity
     * @return このFake自身（メソッドチェーン用）
     */
    public FakeJwtHistoryRepository add(JwtHistory history) {
        if (history.getId() == null) {
            history.setId(nextId++);
        }
        histories.add(history);
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
    public int countByClientId(JwtHistoryCondition condition) {
        return (int) histories.stream()
                .filter(history -> history.getClientId().equals(condition.getClientId()))
                .count();
    }

    /** {@inheritDoc} */
    @Override
    public List<JwtHistory> findByClientId(JwtHistoryCondition condition) {
        Stream<JwtHistory> stream = histories.stream()
                .filter(history -> history.getClientId().equals(condition.getClientId()));

        Option option = condition.getOption();
        boolean asc = option != null && option.getOrderBy() != null;
        boolean sortByMemberId = option != null
                && "member_id".equals(option.getOrderBy() != null ? option.getOrderBy() : option.getOrderByDesc());
        Comparator<JwtHistory> comparator = sortByMemberId
                ? Comparator.comparing(JwtHistory::getMemberId)
                : Comparator.comparing(JwtHistory::getIssueAt);
        stream = stream.sorted(asc ? comparator : comparator.reversed());

        if (option != null && condition.isPaging()) {
            int limit = (int) Math.clamp(option.getLimit(), 1, 500);
            stream = stream.skip(option.getOffset()).limit(limit);
        }
        return stream.toList();
    }

    /** {@inheritDoc} */
    @Override
    public JwtHistory persist(JwtHistory entity) {
        persistCallCount++;
        callLog.add("history");
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        histories.add(entity);
        return entity;
    }
}
