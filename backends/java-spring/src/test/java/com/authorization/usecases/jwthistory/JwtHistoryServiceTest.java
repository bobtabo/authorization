/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.jwthistory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.authorization.domain.client.entities.JwtHistory;
import com.authorization.support.enums.SortType;
import com.authorization.support.fakes.FakeJwtHistoryRepository;
import com.authorization.usecases.jwthistory.dtos.JwtHistoryDto;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@link JwtHistoryService} のユニットテストです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class JwtHistoryServiceTest {

    /**
     * ソート指定が無い場合、実際に発行日時の降順で返され、DTOの表示用メタデータも
     * 降順になることを確認します（メタデータだけでなく実際の並び順を検証する）。
     */
    @Test
    void getHistoriesAppliesDefaultSortWhenUnspecified() {
        LocalDateTime older = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime newer = LocalDateTime.of(2026, 6, 1, 0, 0);
        FakeJwtHistoryRepository repository = new FakeJwtHistoryRepository()
                .add(makeHistory(1L, 1L, older))
                .add(makeHistory(2L, 1L, newer));
        JwtHistoryService service = new JwtHistoryService(repository);

        JwtHistoryDto dto = new JwtHistoryDto();
        dto.setClientId(1L);
        dto.setLimit(10);
        dto.setPaging(1);
        var vo = service.getHistories(dto);

        assertEquals("issue_at", vo.getSort());
        assertEquals(SortType.DESC, vo.getSortType());
        assertEquals(List.of(2L, 1L), vo.getItems().stream().map(JwtHistory::getId).toList());
    }

    /**
     * 指定したクライアントIDに紐づく履歴一覧と件数がそのまま反映されることを確認します。
     */
    @Test
    void getHistoriesReturnsItemsAndCountForClient() {
        FakeJwtHistoryRepository repository = new FakeJwtHistoryRepository()
                .add(makeHistory(1L, 1L))
                .add(makeHistory(2L, 1L))
                .add(makeHistory(3L, 2L));
        JwtHistoryService service = new JwtHistoryService(repository);

        JwtHistoryDto dto = new JwtHistoryDto();
        dto.setClientId(1L);
        dto.setLimit(10);
        dto.setPaging(1);
        var vo = service.getHistories(dto);

        assertEquals(2, vo.getCount());
        assertEquals(2, vo.getItems().size());
    }

    /**
     * テスト用のJWT履歴Entityを組み立てます。
     *
     * @param id JWT履歴ID
     * @param clientId クライアントID
     * @return JWT履歴Entity
     */
    private static JwtHistory makeHistory(long id, long clientId) {
        return makeHistory(id, clientId, LocalDateTime.now());
    }

    /**
     * 発行日時を指定してテスト用のJWT履歴Entityを組み立てます。
     *
     * @param id JWT履歴ID
     * @param clientId クライアントID
     * @param issueAt 発行日時
     * @return JWT履歴Entity
     */
    private static JwtHistory makeHistory(long id, long clientId, LocalDateTime issueAt) {
        JwtHistory history = new JwtHistory();
        history.setId(id);
        history.setClientId(clientId);
        history.setMemberId("member-" + id);
        history.setIssueAt(issueAt);
        history.setJwt("jwt-" + id);
        return history;
    }
}
