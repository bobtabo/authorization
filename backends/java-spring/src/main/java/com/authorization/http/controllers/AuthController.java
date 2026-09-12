/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.http.controllers;

import com.authorization.config.AppConfig;
import com.authorization.domain.staff.enums.Provider;
import com.authorization.domain.staff.valueobjects.StaffVo;
import com.authorization.support.exceptions.AppException;
import com.authorization.support.http.responses.ResponseHelper;
import com.authorization.usecases.auth.AuthService;
import com.authorization.usecases.auth.dtos.AuthUserDto;
import com.authorization.usecases.auth.dtos.SocialDto;
import com.authorization.usecases.invitation.InvitationService;
import com.authorization.usecases.invitation.dtos.InvitationDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 認証Controllerクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@RestController
public class AuthController {

    private static final Duration OAUTH_HTTP_TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient HTTP =
            HttpClient.newBuilder().connectTimeout(OAUTH_HTTP_TIMEOUT).build();
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize";

    private final AuthService authService;
    private final InvitationService invitationService;
    private final AppConfig cfg;

    /**
     * コンストラクタ。
     *
     * @param authService 認証Service
     * @param invitationService 招待Service
     * @param cfg アプリケーション設定
     */
    public AuthController(AuthService authService, InvitationService invitationService, AppConfig cfg) {
        this.authService = authService;
        this.invitationService = invitationService;
        this.cfg = cfg;
    }

    /**
     * ログイン情報を返します（staff_id クッキーで認証済みのユーザー）。
     *
     * @param staffId staff_id クッキーの値
     * @return JSON レスポンス
     */
    @GetMapping("/api/auth/login")
    public ResponseEntity<Map<String, Object>> login(
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long staffId) {
        if (staffId == 0L) {
            throw AppException.unauthorized("unauthenticated");
        }
        AuthUserDto dto = new AuthUserDto();
        dto.setId(staffId);

        StaffVo vo = authService.findUser(dto);
        return ResponseHelper.success(toJson(vo));
    }

    /**
     * 招待トークンを検証し、招待情報を返します。
     *
     * @param token 招待トークン
     * @return JSON レスポンス
     */
    @GetMapping("/api/auth/invitation/{token}")
    public ResponseEntity<Map<String, Object>> invitation(@PathVariable String token) {
        InvitationDto dto = new InvitationDto();
        dto.setToken(token);

        var vo = invitationService.findByToken(dto);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("found", vo.isFound());
        data.put("url", vo.getUrl());
        data.put("display_url", vo.getDisplayUrl());
        data.put("token", vo.getToken());
        return ResponseHelper.success(data);
    }

    /**
     * Google へリダイレクトします。
     *
     * @param token 招待トークン（招待フロー経由の場合のみ）
     * @return リダイレクト応答
     */
    @GetMapping("/auth/google/redirect")
    public ResponseEntity<Void> googleRedirect(@RequestParam(required = false) String token) {
        String state = (token != null && !token.isEmpty()) ? token : "state";
        String url = UriComponentsBuilder.fromUriString(GOOGLE_AUTH_URL)
                .queryParam("client_id", cfg.oauth().googleClientId())
                .queryParam("redirect_uri", cfg.oauth().googleRedirectUrl())
                .queryParam("response_type", "code")
                .queryParam("scope", "email profile")
                .queryParam("access_type", "online")
                .queryParam("state", state)
                .encode()
                .toUriString();
        return redirect(url);
    }

    /**
     * Google からのコールバックを処理します。
     *
     * @param code 認可コード
     * @param state 招待トークン（招待フロー経由の場合のみ、"state" 固定値以外）
     * @return リダイレクト応答
     */
    @GetMapping("/auth/google/callback")
    public ResponseEntity<Void> googleCallback(
            @RequestParam(required = false) String code, @RequestParam(required = false) String state) {
        if (code == null || code.isEmpty()) {
            return errorRedirect(500);
        }
        String invitationToken = (state != null && !state.isEmpty() && !"state".equals(state)) ? state : null;

        try {
            String accessToken = exchangeGoogleCodeForToken(code);
            Map<String, String> userInfo = fetchGoogleUserInfo(accessToken);

            SocialDto dto = new SocialDto();
            dto.setProvider(Provider.Google);
            dto.setProviderId(userInfo.getOrDefault("id", ""));
            dto.setName(userInfo.getOrDefault("name", ""));
            dto.setEmail(userInfo.getOrDefault("email", ""));
            dto.setAvatar(blankToNull(userInfo.get("picture")));
            dto.setInvitationToken(invitationToken);

            StaffVo staff = authService.login(dto);
            return redirectWithStaffCookie(staff.getId());
        } catch (AppException e) {
            return errorRedirect(e.getStatusCode());
        } catch (Exception e) {
            return errorRedirect(500);
        }
    }

    /**
     * GitHub へリダイレクトします。state に "{runtime}|{invitationToken}" を埋め込みます。
     *
     * @param token 招待トークン（招待フロー経由の場合のみ）
     * @return リダイレクト応答
     */
    @GetMapping("/auth/github/redirect")
    public ResponseEntity<Void> githubRedirect(@RequestParam(required = false) String token) {
        String state = (token != null && !token.isEmpty())
                ? cfg.app().runtime() + "|" + token
                : cfg.app().runtime();
        String url = UriComponentsBuilder.fromUriString(GITHUB_AUTHORIZE_URL)
                .queryParam("client_id", cfg.oauth().githubClientId())
                .queryParam("redirect_uri", cfg.oauth().githubRedirectUrl())
                .queryParam("scope", "user:email")
                .queryParam("state", state)
                .encode()
                .toUriString();
        return redirect(url);
    }

    /**
     * GitHub からのコールバックを処理します。state フォーマット: "{runtime}" または
     * "{runtime}|{invitationToken}"。
     *
     * @param code 認可コード
     * @param state ランタイム識別子＋招待トークン
     * @return リダイレクト応答
     */
    @GetMapping("/auth/github/callback")
    public ResponseEntity<Void> githubCallback(
            @RequestParam(required = false) String code, @RequestParam(required = false) String state) {
        if (code == null || code.isEmpty()) {
            return errorRedirect(500);
        }
        String stateVal = state != null ? state : "";
        String[] parts = stateVal.split("\\|", 2);
        String invitationToken = parts.length == 2 && !parts[1].isEmpty() ? parts[1] : null;

        try {
            String accessToken = exchangeGithubCodeForToken(code);
            Map<String, String> userInfo = fetchGithubUserInfo(accessToken);

            SocialDto dto = new SocialDto();
            dto.setProvider(Provider.Github);
            dto.setProviderId(userInfo.getOrDefault("id", ""));
            dto.setName(userInfo.getOrDefault("name", ""));
            dto.setEmail(userInfo.getOrDefault("email", ""));
            dto.setAvatar(blankToNull(userInfo.get("avatar")));
            dto.setInvitationToken(invitationToken);

            StaffVo staff = authService.login(dto);
            return redirectWithStaffCookie(staff.getId());
        } catch (AppException e) {
            return errorRedirect(e.getStatusCode());
        } catch (Exception e) {
            return errorRedirect(500);
        }
    }

    /**
     * 自分自身のプロフィールを返します（staff_id クッキーで認証済みのユーザー）。
     *
     * @param staffId staff_id クッキーの値
     * @return JSON レスポンス
     */
    @GetMapping("/api/auth/me")
    public ResponseEntity<Map<String, Object>> getMyProfile(
            @CookieValue(name = "staff_id", required = false, defaultValue = "0") long staffId) {
        if (staffId == 0L) {
            throw AppException.unauthorized("unauthenticated");
        }
        AuthUserDto dto = new AuthUserDto();
        dto.setId(staffId);

        StaffVo vo = authService.findUser(dto);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("staff_id", vo.getId());
        data.put("name", vo.getName());
        data.put("avatar", vo.getAvatar());
        data.put("role", vo.getRole());
        return ResponseHelper.success(data);
    }

    /**
     * ログアウト処理の応答を返します。
     *
     * @return JSON レスポンス
     */
    @GetMapping("/api/auth/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        ResponseCookie cookie = ResponseCookie.from("staff_id", "").path("/").httpOnly(true).maxAge(0).build();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "SUCCESS");
        return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(body);
    }

    /**
     * staff_id クッキーを付与してクライアント一覧へリダイレクトします。
     *
     * @param staffId スタッフID
     * @return リダイレクト応答
     */
    private ResponseEntity<Void> redirectWithStaffCookie(long staffId) {
        boolean secure = "production".equals(cfg.app().env());
        int maxAge = (int) (cfg.app().staffCookieLifetime() * 60);
        ResponseCookie cookie = ResponseCookie.from("staff_id", String.valueOf(staffId))
                .path("/")
                .httpOnly(true)
                .secure(secure)
                .maxAge(maxAge)
                .build();
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Set-Cookie", cookie.toString())
                .header("Location", cfg.app().frontendUrl() + "/clients")
                .build();
    }

    /**
     * フロントエンドのエラーページへリダイレクトします。
     *
     * @param code エラーコード（HTTPステータスコード相当）
     * @return リダイレクト応答
     */
    private ResponseEntity<Void> errorRedirect(int code) {
        return redirect(cfg.app().frontendUrl() + "/error?code=" + code);
    }

    /**
     * 指定URLへ302リダイレクトします。
     *
     * @param url リダイレクト先URL
     * @return リダイレクト応答
     */
    private static ResponseEntity<Void> redirect(String url) {
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
    }

    /**
     * スタッフ ValueObject を JSON へ変換します。
     *
     * @param vo スタッフValueObject
     * @return レスポンス用マップ
     */
    private static Map<String, Object> toJson(StaffVo vo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", vo.getId());
        m.put("name", vo.getName());
        m.put("email", vo.getEmail());
        m.put("avatar", vo.getAvatar());
        m.put("role", vo.getRole());
        m.put("status", vo.getStatus());
        return m;
    }

    /**
     * Google OAuth の認可コードをアクセストークンに交換します。
     *
     * @param code 認可コード
     * @return アクセストークン
     * @throws Exception HTTP通信・JSON解析に失敗した場合
     */
    private String exchangeGoogleCodeForToken(String code) throws Exception {
        String body = "code=" + encode(code)
                + "&client_id=" + encode(cfg.oauth().googleClientId())
                + "&client_secret=" + encode(cfg.oauth().googleClientSecret())
                + "&redirect_uri=" + encode(cfg.oauth().googleRedirectUrl())
                + "&grant_type=authorization_code";
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token"))
                .timeout(OAUTH_HTTP_TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        JsonNode json = JSON.readTree(HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body());
        return json.path("access_token").asText();
    }

    /**
     * Google のユーザー情報を取得します。
     *
     * @param accessToken Google アクセストークン
     * @return ユーザー情報（id/name/email/picture）
     * @throws Exception HTTP通信・JSON解析に失敗した場合
     */
    private Map<String, String> fetchGoogleUserInfo(String accessToken) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                .timeout(OAUTH_HTTP_TIMEOUT)
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        JsonNode json = JSON.readTree(HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body());
        Map<String, String> m = new LinkedHashMap<>();
        m.put("id", json.path("id").asText(""));
        m.put("name", json.path("name").asText(""));
        m.put("email", json.path("email").asText(""));
        m.put("picture", json.path("picture").asText(""));
        return m;
    }

    /**
     * GitHub OAuth の認可コードをアクセストークンに交換します。
     *
     * @param code 認可コード
     * @return アクセストークン
     * @throws Exception HTTP通信・JSON解析に失敗した場合
     */
    private String exchangeGithubCodeForToken(String code) throws Exception {
        String body = "client_id=" + encode(cfg.oauth().githubClientId())
                + "&client_secret=" + encode(cfg.oauth().githubClientSecret())
                + "&code=" + encode(code);
        HttpRequest req = HttpRequest.newBuilder(URI.create("https://github.com/login/oauth/access_token"))
                .timeout(OAUTH_HTTP_TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        JsonNode json = JSON.readTree(HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body());
        return json.path("access_token").asText();
    }

    /**
     * GitHub のユーザー情報を取得します。email が非公開の場合は emails API から
     * primary アドレスを取得します。
     *
     * @param accessToken GitHub アクセストークン
     * @return ユーザー情報（id/name/email/avatar）
     * @throws Exception HTTP通信・JSON解析に失敗した場合
     */
    private Map<String, String> fetchGithubUserInfo(String accessToken) throws Exception {
        HttpRequest userReq = HttpRequest.newBuilder(URI.create("https://api.github.com/user"))
                .timeout(OAUTH_HTTP_TIMEOUT)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();
        JsonNode userJson = JSON.readTree(HTTP.send(userReq, HttpResponse.BodyHandlers.ofString()).body());

        String name = userJson.path("name").asText("");
        if (name.isEmpty()) {
            name = userJson.path("login").asText("");
        }
        String id = userJson.path("id").asText("");
        String avatar = userJson.path("avatar_url").asText("");

        String email = userJson.path("email").asText("");
        if (email.isEmpty()) {
            HttpRequest emailReq = HttpRequest.newBuilder(URI.create("https://api.github.com/user/emails"))
                    .timeout(OAUTH_HTTP_TIMEOUT)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            JsonNode emails = JSON.readTree(HTTP.send(emailReq, HttpResponse.BodyHandlers.ofString()).body());
            for (JsonNode e : emails) {
                if (e.path("primary").asBoolean(false)) {
                    email = e.path("email").asText("");
                    break;
                }
            }
        }

        Map<String, String> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("email", email);
        m.put("avatar", avatar);
        return m;
    }

    /**
     * 空文字列を null に変換します。
     *
     * @param s 対象文字列
     * @return 空文字列でなければそのまま、空文字列またはnullならnull
     */
    private static String blankToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    /**
     * URLエンコードします（UTF-8）。
     *
     * @param s 対象文字列
     * @return エンコード後の文字列
     */
    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
