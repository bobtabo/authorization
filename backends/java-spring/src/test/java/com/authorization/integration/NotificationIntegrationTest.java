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
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * 通知（Notification）APIの実DB/Redis接続統合テストです。
 * kotlin-ktorの{@code integration/NotificationIntegrationTest.kt}に相当します。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class NotificationIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        TestHelper.truncateTables();
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void countsReturnsUnreadAndTotalWhenAuthenticated() {
        var staff = TestHelper.createStaff();
        TestHelper.createNotification(staff.id(), "通知1");
        TestHelper.createNotification(staff.id(), "通知2", true);

        EntityExchangeResult<Map> result = client.get()
                .uri("/api/notifications/counts")
                .header("Cookie", "staff_id=" + staff.id())
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(((Number) result.getResponseBody().get("total")).longValue()).isEqualTo(2L);
        assertThat(((Number) result.getResponseBody().get("unread")).longValue()).isEqualTo(1L);
    }

    @Test
    void countsReturns401WhenUnauthenticated() {
        EntityExchangeResult<Map> result = client.get()
                .uri("/api/notifications/counts")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(401);
    }

    @Test
    void indexReturnsNotificationListWhenAuthenticated() {
        // limit未指定時のデフォルトは1件（NotificationController.index参照）のため、
        // 2件とも取得できることを確認するには明示的にlimitを指定する。
        var staff = TestHelper.createStaff();
        TestHelper.createNotification(staff.id(), "通知1");
        TestHelper.createNotification(staff.id(), "通知2");

        EntityExchangeResult<Map> result = client.get()
                .uri("/api/notifications?limit=10")
                .header("Cookie", "staff_id=" + staff.id())
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat((List<?>) result.getResponseBody().get("items")).hasSize(2);
    }

    @Test
    void indexReturns401WhenUnauthenticated() {
        EntityExchangeResult<Map> result = client.get()
                .uri("/api/notifications")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(401);
    }

    @Test
    void readAllMarksReadWhenAuthenticated() {
        var staff = TestHelper.createStaff();
        TestHelper.createNotification(staff.id(), "通知1");
        TestHelper.createNotification(staff.id(), "通知2");

        EntityExchangeResult<Map> result = client.patch()
                .uri("/api/notifications")
                .header("Cookie", "staff_id=" + staff.id())
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
    }

    @Test
    void readAllReturns401WhenUnauthenticated() {
        EntityExchangeResult<Map> result = client.patch()
                .uri("/api/notifications")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(401);
    }

    @Test
    void readMarksSingleNotificationAsRead() {
        var staff = TestHelper.createStaff();
        var notif = TestHelper.createNotification(staff.id(), "通知1");

        EntityExchangeResult<Map> result = client.patch()
                .uri("/api/notifications/" + notif.id())
                .header("Cookie", "staff_id=" + staff.id())
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(((Number) result.getResponseBody().get("id")).longValue()).isEqualTo(notif.id());
    }
}
