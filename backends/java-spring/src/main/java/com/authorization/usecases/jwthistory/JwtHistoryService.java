/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.jwthistory;

import com.authorization.domain.client.condition.JwtHistoryCondition;
import com.authorization.domain.client.entities.JwtHistory;
import com.authorization.domain.client.repositories.JwtHistoryRepository;
import com.authorization.domain.client.valueobjects.JwtHistoryListVo;
import com.authorization.support.enums.SortType;
import com.authorization.support.repositories.conditions.Option;
import com.authorization.support.services.AbstractService;
import com.authorization.usecases.jwthistory.dtos.JwtHistoryDto;
import java.util.List;

/**
 * JWT履歴Serviceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class JwtHistoryService extends AbstractService {

    private final JwtHistoryRepository repository;

    /**
     * コンストラクタ。
     *
     * @param repository JWT履歴Repository
     */
    public JwtHistoryService(JwtHistoryRepository repository) {
        this.repository = repository;
    }

    /**
     * クライアントに紐づくJWT履歴一覧を返します。
     *
     * @param dto JWT履歴DTO
     * @return JWT履歴一覧ValueObject
     */
    public JwtHistoryListVo getHistories(JwtHistoryDto dto) {
        if (dto.getSort() == null || dto.getSort().isEmpty()) {
            dto.setSort("issue_at");
        }
        if (dto.getSortType() == SortType.NONE) {
            dto.setSortType(SortType.DESC);
        }

        JwtHistoryCondition condition = new JwtHistoryCondition();
        condition.setClientId(dto.getClientId());
        condition.setOption(new Option(dto.getOffset(), dto.getLimit(), dto.getSort(), dto.getSortType()));

        int count = repository.countByClientId(condition);
        List<JwtHistory> list = repository.findByClientId(condition);

        JwtHistoryListVo vo = new JwtHistoryListVo();
        vo.setItems(list);
        vo.setCount(count);
        vo.setOffset(dto.getOffset());
        vo.setLimit(dto.getLimit());
        vo.setSort(dto.getSort());
        vo.setSortType(dto.getSortType());
        return vo;
    }
}
