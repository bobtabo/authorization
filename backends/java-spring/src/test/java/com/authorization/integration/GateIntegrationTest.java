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
 * Gate（JWT発行・検証）APIの実DB/Redis接続統合テストです。
 * kotlin-ktorの{@code integration/GateIntegrationTest.kt}に相当します。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class GateIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        TestHelper.truncateTables();
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void issueReturnsJwtTokenForValidClient() {
        var c = TestHelper.createClient();

        EntityExchangeResult<Map> result = client.get()
                .uri("/api/gate/issue?member=user-001")
                .header("Authorization", "Bearer " + c.accessToken())
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).containsKey("token");
        assertThat((String) result.getResponseBody().get("token")).isNotBlank();
    }

    @Test
    void issueReturns400WhenMemberParamEmpty() {
        // member は @RequestParam String（required 省略＝必須）のため、パラメーター自体を
        // 省略すると Spring 側のバインディングでコントローラー到達前に400になる。
        // コントローラー自身の member_required 検証を通すため、空文字で明示的に渡す。
        EntityExchangeResult<Map> result = client.get()
                .uri("/api/gate/issue?member=")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(400);
        assertThat(result.getResponseBody()).containsEntry("message", "member_required");
    }

    @Test
    void verifyReturnsClaimsForValidToken() {
        var c = TestHelper.createClient();
        EntityExchangeResult<Map> issueResult = client.get()
                .uri("/api/gate/issue?member=user-001")
                .header("Authorization", "Bearer " + c.accessToken())
                .exchange()
                .expectBody(Map.class)
                .returnResult();
        String token = (String) issueResult.getResponseBody().get("token");

        EntityExchangeResult<Map> result = client.get()
                .uri("/api/gate/client/" + c.identifier() + "/verify?token=" + token)
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).containsEntry("sub", "user-001");
    }

    @Test
    void verifyReturns400WhenTokenEmpty() {
        // token も @RequestParam String（必須）のため、省略ではなく空文字で明示的に渡し、
        // コントローラー自身の token_required 検証を通す（issueReturns400WhenMemberParamEmpty同様の理由）。
        var c = TestHelper.createClient();

        EntityExchangeResult<Map> result = client.get()
                .uri("/api/gate/client/" + c.identifier() + "/verify?token=")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(400);
        assertThat(result.getResponseBody()).containsEntry("message", "token_required");
    }
}
