/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.usecases.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.authorization.domain.client.entities.Client;
import com.authorization.domain.client.enums.ClientStatus;
import com.authorization.domain.client.mappers.ClientApiMapperImpl;
import com.authorization.domain.client.mappers.ClientConditionMapperImpl;
import com.authorization.domain.client.mappers.ClientDtoMapperImpl;
import com.authorization.domain.client.valueobjects.ClientStartVo;
import com.authorization.domain.client.valueobjects.ClientStoreVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.fakes.FakeClientRepository;
import com.authorization.usecases.client.dtos.ClientDto;
import org.junit.jupiter.api.Test;

/**
 * {@link ClientService} のユニットテストです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
class ClientServiceTest {

    /**
     * 対象クライアントが存在しない場合、404（client_not_found）を投げることを確認します。
     */
    @Test
    void showThrowsNotFoundWhenClientDoesNotExist() {
        ClientService service = newService(new FakeClientRepository());

        ClientDto dto = new ClientDto();
        dto.setId(99L);

        AppException exception = assertThrows(AppException.class, () -> service.show(dto));

        assertEquals(404, exception.getStatusCode());
        assertEquals("client_not_found", exception.getMessage());
    }

    /**
     * 登録すると鍵情報とアクセストークンが生成され、初期状態が Inactive になることを確認します。
     */
    @Test
    void storeGeneratesKeysAndSetsInactiveStatus() {
        FakeClientRepository repository = new FakeClientRepository();
        ClientService service = newService(repository);

        ClientDto dto = storeDto();
        ClientStoreVo vo = service.store(dto);

        assertNotNull(vo.getId());
        Client saved = repository.findById(conditionOf(vo.getId()));
        assertNotNull(saved.getPrivateKey());
        assertNotNull(saved.getPublicKey());
        assertNotNull(saved.getAccessToken());
        assertEquals(ClientStatus.Inactive, saved.getStatus());
    }

    /**
     * 対象クライアントが存在しない場合、404（client_not_found）を投げることを確認します。
     */
    @Test
    void updateThrowsNotFoundWhenClientDoesNotExist() {
        ClientService service = newService(new FakeClientRepository());

        ClientDto dto = new ClientDto();
        dto.setId(99L);
        AppException exception = assertThrows(AppException.class, () -> service.update(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * versionが一致しない場合、409（optimistic_lock）を投げることを確認します。
     */
    @Test
    void updateThrowsConflictWhenVersionIsStale() {
        Client client = makeClient(1L, ClientStatus.Inactive);
        client.setVersion(3);
        ClientService service = newService(new FakeClientRepository().add(client));

        ClientDto dto = new ClientDto();
        dto.setId(1L);
        dto.setVersion(1);
        AppException exception = assertThrows(AppException.class, () -> service.update(dto));

        assertEquals(409, exception.getStatusCode());
        assertEquals("optimistic_lock", exception.getMessage());
    }

    /**
     * ステータスをActiveへ更新すると、start_atが未設定なら現在時刻が設定されることを確認します。
     */
    @Test
    void updateSetsStartAtWhenTransitioningToActive() {
        Client client = makeClient(1L, ClientStatus.Inactive);
        FakeClientRepository repository = new FakeClientRepository().add(client);
        ClientService service = newService(repository);

        ClientDto dto = new ClientDto();
        dto.setId(1L);
        dto.setVersion(1);
        dto.setStatus(ClientStatus.Active);
        service.update(dto);

        assertNotNull(repository.findById(conditionOf(1L)).getStartAt());
        assertEquals(1, repository.getPersistCallCount());
    }

    /**
     * 対象クライアントが存在しない場合、404（client_not_found）を投げることを確認します。
     */
    @Test
    void destroyThrowsNotFoundWhenClientDoesNotExist() {
        ClientService service = newService(new FakeClientRepository());

        ClientDto dto = new ClientDto();
        dto.setId(99L);
        AppException exception = assertThrows(AppException.class, () -> service.destroy(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * versionが一致しない場合、409（optimistic_lock）を投げることを確認します
     * （ステータスをClosedへ更新する前にバージョンチェックが行われることを確認）。
     */
    @Test
    void destroyThrowsConflictWhenVersionIsStale() {
        Client client = makeClient(1L, ClientStatus.Active);
        client.setVersion(3);
        FakeClientRepository repository = new FakeClientRepository().add(client);
        ClientService service = newService(repository);

        ClientDto dto = new ClientDto();
        dto.setId(1L);
        dto.setVersion(1);
        AppException exception = assertThrows(AppException.class, () -> service.destroy(dto));

        assertEquals(409, exception.getStatusCode());
        assertEquals(ClientStatus.Active, repository.findById(conditionOf(1L)).getStatus());
    }

    /**
     * 正しい入力の場合、Closedへ更新してから論理削除することを確認します。
     */
    @Test
    void destroyClosesThenSoftDeletes() {
        Client client = makeClient(1L, ClientStatus.Active);
        FakeClientRepository repository = new FakeClientRepository().add(client);
        ClientService service = newService(repository);

        ClientDto dto = new ClientDto();
        dto.setId(1L);
        dto.setVersion(1);
        service.destroy(dto);

        Client saved = repository.findById(conditionOf(1L));
        assertEquals(ClientStatus.Closed, saved.getStatus());
        assertNotNull(saved.getDeletedAt());
        assertEquals(1, repository.getPersistCallCount());
        assertEquals(1, repository.getDeleteByIdCallCount());
    }

    /**
     * 識別名に対応するクライアントが存在しない場合、404（client_not_found）を投げることを確認します。
     */
    @Test
    void startThrowsNotFoundWhenClientDoesNotExist() {
        ClientService service = newService(new FakeClientRepository());

        ClientDto dto = new ClientDto();
        dto.setIdentifier("unknown");
        AppException exception = assertThrows(AppException.class, () -> service.start(dto));

        assertEquals(404, exception.getStatusCode());
    }

    /**
     * Inactiveのクライアントを利用開始すると、Activeへ遷移してアクセストークンを返すことを確認します。
     */
    @Test
    void startActivatesInactiveClientAndReturnsAccessToken() {
        Client client = makeClient(1L, ClientStatus.Inactive);
        client.setIdentifier("client-1");
        client.setAccessToken("token-1");
        FakeClientRepository repository = new FakeClientRepository().add(client);
        ClientService service = newService(repository);

        ClientDto dto = new ClientDto();
        dto.setIdentifier("client-1");
        ClientStartVo vo = service.start(dto);

        assertEquals("token-1", vo.getAccessToken());
        assertEquals(ClientStatus.Active, repository.findById(conditionOf(1L)).getStatus());
        assertEquals(1, repository.getPersistCallCount());
    }

    /**
     * Activeのクライアントを利用停止すると、Suspendedへ遷移することを確認します。
     */
    @Test
    void stopSuspendsActiveClient() {
        Client client = makeClient(1L, ClientStatus.Active);
        client.setIdentifier("client-1");
        FakeClientRepository repository = new FakeClientRepository().add(client);
        ClientService service = newService(repository);

        ClientDto dto = new ClientDto();
        dto.setIdentifier("client-1");
        service.stop(dto);

        assertEquals(ClientStatus.Suspended, repository.findById(conditionOf(1L)).getStatus());
        assertEquals(1, repository.getPersistCallCount());
    }

    /**
     * テスト用のClientServiceを組み立てます。
     *
     * @param repository クライアントRepository（Fake）
     * @return ClientService
     */
    private static ClientService newService(FakeClientRepository repository) {
        return new ClientService(
                repository, new ClientConditionMapperImpl(), new ClientApiMapperImpl(), new ClientDtoMapperImpl());
    }

    /**
     * IDのみを設定した検索条件を組み立てます。
     *
     * @param id クライアントID
     * @return クライアント検索条件
     */
    private static com.authorization.domain.client.condition.ClientCondition conditionOf(long id) {
        com.authorization.domain.client.condition.ClientCondition condition =
                new com.authorization.domain.client.condition.ClientCondition();
        condition.setId(id);
        return condition;
    }

    /**
     * 登録用のクライアントDTOを組み立てます。
     *
     * @return クライアントDTO
     */
    private static ClientDto storeDto() {
        ClientDto dto = new ClientDto();
        dto.setName("Client");
        dto.setPostCode("1000001");
        dto.setPref("東京都");
        dto.setCity("千代田区");
        dto.setAddress("1-1-1");
        dto.setTel("0312345678");
        dto.setEmail("client@example.com");
        dto.setExecutorId(9L);
        return dto;
    }

    /**
     * テスト用のクライアントEntityを組み立てます。
     *
     * @param id クライアントID
     * @param status ステータス
     * @return クライアントEntity
     */
    private static Client makeClient(long id, ClientStatus status) {
        Client client = new Client();
        client.setId(id);
        client.setName("Client " + id);
        client.setStatus(status);
        client.setVersion(1);
        return client;
    }
}
