/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.authorization.Application;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * 管理招待（AdminInvitation）APIの実DB/Redis接続統合テストです。
 * kotlin-ktorの{@code integration/AdminInvitationIntegrationTest.kt}に相当します。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class AdminInvitationIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        TestHelper.truncateTables();
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void indexReturnsCurrentInvitation() {
        var inv = TestHelper.createInvitation();

        EntityExchangeResult<Map> result = client.get()
                .uri("/api/admin/invitation?role=2")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).containsEntry("found", true);
        assertThat(result.getResponseBody()).containsEntry("token", inv.token());
    }

    @Test
    void issueIssuesNewInvitation() {
        // issue() は既存の招待行のトークンを再生成する実装（新規行は作らない）ため、
        // 対象ロールの招待が事前に存在している必要がある。
        TestHelper.createInvitation();
        var staff = TestHelper.createStaff();

        EntityExchangeResult<Map> result = client.get()
                .uri("/api/admin/invitation/issue?role=2")
                .header("Cookie", "staff_id=" + staff.id())
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).containsEntry("found", true);
        assertThat((String) result.getResponseBody().get("token")).isNotBlank();
    }

    @Test
    void issueReturns401WhenUnauthenticated() {
        EntityExchangeResult<Map> result = client.get()
                .uri("/api/admin/invitation/issue?role=2")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(401);
    }

    @Test
    void indexReturns400ForInvalidRole() {
        EntityExchangeResult<Map> result = client.get()
                .uri("/api/admin/invitation?role=3")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(400);
    }
}
