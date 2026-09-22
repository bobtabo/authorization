/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.authorization.Application;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.JdkClientHttpRequestFactory;
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

    private RestTestClient noRedirectClient() {
        var jdk = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
        return RestTestClient.bindToServer(new JdkClientHttpRequestFactory(jdk))
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void githubRedirectIssuesNonceCookieAndEmbedsItInState() {
        EntityExchangeResult<byte[]> result = noRedirectClient().get()
                .uri("/auth/github/redirect?token=inv-token")
                .exchange()
                .expectBody()
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(302);
        List<String> cookies = result.getResponseHeaders().get("Set-Cookie");
        String oauthCookie = cookies.stream().filter(c -> c.startsWith("oauth_state=")).findFirst().orElseThrow();
        Matcher m = Pattern.compile("oauth_state=([0-9a-f]+)").matcher(oauthCookie);
        assertThat(m.find()).isTrue();
        assertThat(oauthCookie).contains("HttpOnly");
        String expected = URLEncoder.encode("java|" + m.group(1) + "|inv-token", StandardCharsets.UTF_8);
        assertThat(result.getResponseHeaders().getFirst("Location")).contains(expected);
    }

    @Test
    void githubCallbackWithoutNonceCookieRedirectsTo400() {
        EntityExchangeResult<byte[]> result = noRedirectClient().get()
                .uri("/auth/github/callback?code=abc&state=java%7Cnonce123")
                .exchange()
                .expectBody()
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(302);
        assertThat(result.getResponseHeaders().getFirst("Location")).endsWith("/error?code=400");
    }

    @Test
    void googleCallbackWithMismatchedNonceRedirectsTo400AndClearsCookie() {
        EntityExchangeResult<byte[]> result = noRedirectClient().get()
                .uri("/auth/google/callback?code=abc&state=java%7Cwrong")
                .header("Cookie", "oauth_state=right")
                .exchange()
                .expectBody()
                .returnResult();

        assertThat(result.getStatus().value()).isEqualTo(302);
        assertThat(result.getResponseHeaders().getFirst("Location")).endsWith("/error?code=400");
        assertThat(result.getResponseHeaders().get("Set-Cookie"))
                .anyMatch(c -> c.startsWith("oauth_state=;") && c.contains("Max-Age=0"));
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
