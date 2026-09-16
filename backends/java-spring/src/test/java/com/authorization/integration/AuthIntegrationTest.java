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
 * 認証（Auth）APIの実DB/Redis接続統合テストです。
 * kotlin-ktorの{@code integration/AuthIntegrationTest.kt}に相当します。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        TestHelper.truncateTables();
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void meReturnsProfileWhenAuthenticated() {
        var staff = TestHelper.createStaff();

        EntityExchangeResult<Map> result = client.get()
                .uri("/api/auth/me")
                .header("Cookie", "staff_id=" + staff.id())
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(((Number) result.getResponseBody().get("staff_id")).longValue()).isEqualTo(staff.id());
    }

    @Test
    void meReturns401WhenUnauthenticated() {
        EntityExchangeResult<Map> result = client.get()
                .uri("/api/auth/me")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(401);
    }

    @Test
    void loginReturnsInfoWhenAuthenticated() {
        var staff = TestHelper.createStaff();

        EntityExchangeResult<Map> result = client.get()
                .uri("/api/auth/login")
                .header("Cookie", "staff_id=" + staff.id())
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        // StaffVoはid（staff_idではない）フィールドをそのままJSON化するため、キー名が/meと異なる。
        assertThat(((Number) result.getResponseBody().get("id")).longValue()).isEqualTo(staff.id());
    }

    @Test
    void loginReturns401WhenUnauthenticated() {
        EntityExchangeResult<Map> result = client.get()
                .uri("/api/auth/login")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(401);
    }

    @Test
    void logoutReturns200() {
        EntityExchangeResult<Map> result = client.get()
                .uri("/api/auth/logout")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
    }

    @Test
    void invitationReturnsInvitationWhenValidToken() {
        var inv = TestHelper.createInvitation("test-token-xyz", 2);

        EntityExchangeResult<Map> result = client.get()
                .uri("/api/auth/invitation/" + inv.token())
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).containsEntry("token", inv.token());
        assertThat(result.getResponseBody()).containsEntry("found", true);
    }
}
