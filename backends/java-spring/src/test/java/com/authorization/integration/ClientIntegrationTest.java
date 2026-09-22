/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.authorization.Application;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * クライアント（Client）APIの実DB/Redis接続統合テストです。
 * kotlin-ktorの{@code integration/ClientIntegrationTest.kt}に相当します。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class ClientIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        TestHelper.truncateTables();
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void indexReturnsList() {
        TestHelper.createClient();
        TestHelper.createClient();

        EntityExchangeResult<Map> result = client.get().uri("/api/clients").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat((List<?>) result.getResponseBody().get("data")).hasSize(2);
    }

    @Test
    void indexReturnsEmptyListWhenNoneExist() {
        EntityExchangeResult<Map> result = client.get().uri("/api/clients").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat((List<?>) result.getResponseBody().get("data")).isEmpty();
    }

    @Test
    void indexKeywordPercentIsNotTreatedAsWildcard() {
        TestHelper.createClient("50%割引プラン");
        TestHelper.createClient("50個セット");

        EntityExchangeResult<Map> result = client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/clients").queryParam("keyword", "50%").build())
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat((List<?>) result.getResponseBody().get("data")).hasSize(1);
    }

    @Test
    void showReturnsClientDetail() {
        var c = TestHelper.createClient();

        EntityExchangeResult<Map> result = client.get().uri("/api/clients/" + c.id()).exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).containsEntry("identifier", c.identifier());
    }

    @Test
    void showNonexistentIdReturnsError() {
        EntityExchangeResult<Map> result = client.get().uri("/api/clients/99999").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(404);
    }

    @Test
    void storeRegistersClientAndReturns201() {
        var staff = TestHelper.createStaff();

        EntityExchangeResult<Map> result = client.post()
                .uri("/api/clients/store")
                .header("Cookie", "staff_id=" + TestHelper.signStaffCookie(staff.id()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "name", "新規クライアント株式会社",
                        "post_code", "100-0001",
                        "pref", "東京都",
                        "city", "千代田区",
                        "address", "千代田1-1",
                        "tel", "0312345678",
                        "email", "new-client@example.com"))
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(201);
        assertThat(result.getResponseBody()).containsKey("id");
    }

    @Test
    void updateUpdatesClient() {
        var staff = TestHelper.createStaff();
        var c = TestHelper.createClient();

        EntityExchangeResult<Map> result = client.put()
                .uri("/api/clients/" + c.id() + "/update")
                .header("Cookie", "staff_id=" + TestHelper.signStaffCookie(staff.id()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", "更新後クライアント名", "version", c.version()))
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).containsEntry("name", "更新後クライアント名");
    }

    @Test
    void destroyRemovesClient() {
        var staff = TestHelper.createStaff();
        var c = TestHelper.createClient();

        EntityExchangeResult<Map> result = client.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/clients/" + c.id() + "/delete")
                .header("Cookie", "staff_id=" + TestHelper.signStaffCookie(staff.id()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("version", c.version()))
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
    }

    // --- スマホ連携 API ---

    @Test
    void qrReturnsQrData() {
        var c = TestHelper.createClient();

        EntityExchangeResult<Map> result = client.get().uri("/api/clients/" + c.identifier() + "/qr").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).containsEntry("identifier", c.identifier());
        assertThat(result.getResponseBody()).containsEntry("deeplink_url", "authgateway://clients/" + c.identifier() + "/info");
    }

    @Test
    void qrNonexistentIdentifierReturnsError() {
        EntityExchangeResult<Map> result = client.get().uri("/api/clients/nonexistent-identifier/qr").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(404);
    }

    @Test
    void infoReturnsClientInfo() {
        var c = TestHelper.createClient();

        EntityExchangeResult<Map> result = client.get().uri("/api/clients/" + c.identifier() + "/info").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).containsEntry("identifier", c.identifier());
        assertThat(result.getResponseBody()).containsKey("name");
        assertThat(result.getResponseBody()).containsKey("status");
    }

    @Test
    void infoNonexistentIdentifierReturnsError() {
        EntityExchangeResult<Map> result = client.get().uri("/api/clients/nonexistent-identifier/info").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(404);
    }

    @Test
    void startReturnsAccessToken() {
        var c = TestHelper.createClient();

        EntityExchangeResult<Map> result = client.patch().uri("/api/clients/" + c.identifier() + "/start").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(result.getResponseBody()).containsKey("access_token");
    }

    @Test
    void startNonexistentIdentifierReturnsError() {
        EntityExchangeResult<Map> result = client.patch().uri("/api/clients/nonexistent-identifier/start").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(404);
    }

    @Test
    void stopReturnsSuccessEnvelopeOnly() {
        var c = TestHelper.createClient();

        EntityExchangeResult<Map> result = client.patch().uri("/api/clients/" + c.identifier() + "/stop").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        // ResponseHelper.success(Map.of()) は message envelope のみを付与するため、
        // 完全な空Mapにはならない（kotlin-ktor版とはこの点で応答形が異なる）。
        assertThat(result.getResponseBody()).containsEntry("message", "SUCCESS").hasSize(1);
    }

    @Test
    void stopNonexistentIdentifierReturnsError() {
        EntityExchangeResult<Map> result = client.patch().uri("/api/clients/nonexistent-identifier/stop").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(404);
    }
}
