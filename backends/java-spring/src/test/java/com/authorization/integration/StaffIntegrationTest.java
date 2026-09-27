/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.integration;

import static com.authorization.jooq.Tables.STAFFS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.authorization.Application;
import java.time.LocalDateTime;
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
 * スタッフ（Staff）APIの実DB/Redis接続統合テストです。
 * kotlin-ktorの{@code integration/StaffIntegrationTest.kt}に相当します。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class StaffIntegrationTest {

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
        TestHelper.createStaff("s1@example.com", 1);
        TestHelper.createStaff("s2@example.com", 1);

        EntityExchangeResult<Map> result = client.get().uri("/api/staffs").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat((List<?>) result.getResponseBody().get("data")).hasSize(2);
        assertThat(result.getResponseBody()).containsKey("pager");
    }

    @Test
    void indexReturnsEmptyListWhenNoneExist() {
        EntityExchangeResult<Map> result = client.get().uri("/api/staffs").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat((List<?>) result.getResponseBody().get("data")).isEmpty();
    }

    @Test
    void indexKeywordUnderscoreIsNotTreatedAsWildcard() {
        TestHelper.createStaff("アンダースコア", "a_b@example.com", 1);
        TestHelper.createStaff("エックス", "axb@example.com", 1);

        EntityExchangeResult<Map> result =
                client.get().uri("/api/staffs?keyword=a_b").exchange().expectBody(Map.class).returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat((List<?>) result.getResponseBody().get("data")).hasSize(1);
    }

    @Test
    void updateRoleUpdatesRoleAndReturnsId() {
        var target = TestHelper.createStaff("target@example.com", 2);
        var executor = TestHelper.createStaff("exec@example.com", 1);

        EntityExchangeResult<Map> result = client.patch()
                .uri("/api/staffs/" + target.id() + "/updateRole")
                .header("Cookie", "staff_id=" + TestHelper.signStaffCookie(executor.id()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("role", 1))
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(((Number) result.getResponseBody().get("id")).longValue()).isEqualTo(target.id());
        Long persistedRole = TestHelper.dsl().select(STAFFS.ROLE).from(STAFFS)
                .where(STAFFS.ID.eq(target.id())).fetchOne(STAFFS.ROLE);
        assertThat(persistedRole).isEqualTo(1L);
    }

    @Test
    void updateRoleUnauthenticatedReturns401() {
        var target = TestHelper.createStaff("target-unauth@example.com", 2);

        EntityExchangeResult<Map> result = client.patch()
                .uri("/api/staffs/" + target.id() + "/updateRole")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("role", 1))
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(401);
    }

    @Test
    void updateRoleNonAdminExecutorReturns403() {
        var target = TestHelper.createStaff("target-member@example.com", 2);
        var executor = TestHelper.createStaff("member-executor@example.com", 2);

        EntityExchangeResult<Map> result = client.patch()
                .uri("/api/staffs/" + target.id() + "/updateRole")
                .header("Cookie", "staff_id=" + TestHelper.signStaffCookie(executor.id()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("role", 1))
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(403);
    }

    @Test
    void updateRoleDeletedAdminExecutorReturns403() {
        var target = TestHelper.createStaff("target-deleted-admin@example.com", 2);
        var executor = TestHelper.createStaff("deleted-admin-executor@example.com", 1);
        // 署名済みクッキーは有効だが、実行者は既に無効化（論理削除）されている状態を再現する。
        TestHelper.dsl().update(STAFFS)
                .set(STAFFS.DELETED_AT, LocalDateTime.now())
                .where(STAFFS.ID.eq(executor.id()))
                .execute();

        EntityExchangeResult<Map> result = client.patch()
                .uri("/api/staffs/" + target.id() + "/updateRole")
                .header("Cookie", "staff_id=" + TestHelper.signStaffCookie(executor.id()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("role", 1))
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(403);
    }

    @Test
    void restoreRestoresDeletedStaffAndReturnsId() {
        var staff = TestHelper.createStaff();
        TestHelper.dsl().update(STAFFS)
                .set(STAFFS.DELETED_AT, LocalDateTime.now())
                .where(STAFFS.ID.eq(staff.id()))
                .execute();

        EntityExchangeResult<Map> result = client.patch()
                .uri("/api/staffs/" + staff.id() + "/restore")
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(((Number) result.getResponseBody().get("id")).longValue()).isEqualTo(staff.id());
        LocalDateTime deletedAt = TestHelper.dsl().select(STAFFS.DELETED_AT).from(STAFFS)
                .where(STAFFS.ID.eq(staff.id())).fetchOne(STAFFS.DELETED_AT);
        assertThat(deletedAt).isNull();
    }

    @Test
    void destroySoftDeletesStaffAndReturnsId() {
        var executor = TestHelper.createStaff("exec@example.com", 1);
        var target = TestHelper.createStaff("target@example.com", 2);

        EntityExchangeResult<Map> result = client.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/staffs/" + target.id() + "/delete")
                .header("Cookie", "staff_id=" + TestHelper.signStaffCookie(executor.id()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("version", 1))
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(((Number) result.getResponseBody().get("id")).longValue()).isEqualTo(target.id());
        LocalDateTime deletedAt = TestHelper.dsl().select(STAFFS.DELETED_AT).from(STAFFS)
                .where(STAFFS.ID.eq(target.id())).fetchOne(STAFFS.DELETED_AT);
        assertThat(deletedAt).isNotNull();
    }

    @Test
    void destroyReturns401WhenUnauthenticated() {
        var target = TestHelper.createStaff("target-unauth@example.com", 2);

        EntityExchangeResult<Map> result = client.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/api/staffs/" + target.id() + "/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("version", 1))
                .exchange()
                .expectBody(Map.class)
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(401);
    }
}
