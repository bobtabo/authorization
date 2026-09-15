/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.integration;

import static com.authorization.jooq.Tables.CLIENTS;
import static com.authorization.jooq.Tables.INVITATIONS;
import static com.authorization.jooq.Tables.NOTIFICATIONS;
import static com.authorization.jooq.Tables.STAFFS;

import com.authorization.config.AppConfig;
import com.authorization.config.ConfigLoader;
import com.authorization.config.DbConfig;
import com.authorization.infrastructure.cache.RedisConfig;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import redis.clients.jedis.JedisPool;

/**
 * 統合テスト用に実DB（MySQL）・実Redisへ接続し、テストデータを作成するヘルパーです。
 * kotlin-ktorの{@code integration/TestHelper.kt}に相当します。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public final class TestHelper {

    /** スタッフ行です。 */
    public record StaffRow(long id, String email) {}

    /** クライアント行です。 */
    public record ClientRow(long id, String identifier, String accessToken, int version) {}

    /** 招待行です。 */
    public record InvitationRow(int id, String token) {}

    /** 通知行です。 */
    public record NotificationRow(long id, long staffId) {}

    /** アプリケーション設定（本番と同じ{@link ConfigLoader}経由で読み込む）です。 */
    public static final AppConfig CFG = new ConfigLoader().appConfig();

    private static final DataSource DATA_SOURCE = new DbConfig().dataSource(CFG);
    private static final DSLContext DSL = new DbConfig().dslContext(DATA_SOURCE);
    private static final JedisPool JEDIS_POOL = new RedisConfig().jedisPool(CFG);

    // RSA 4096bit の生成は重いため、テスト全体で 1 回だけ 2048bit 鍵を生成して再利用する
    // （JWT 署名検証の往復ができれば十分で、鍵長は本番と一致させる必要が無いため）。
    private static final KeyPair CACHED_KEY_PAIR = generateKeyPair();
    private static final String CACHED_PRIVATE_KEY_PEM = toPem("PRIVATE KEY", CACHED_KEY_PAIR.getPrivate().getEncoded());
    private static final String CACHED_PUBLIC_KEY_PEM = toPem("PUBLIC KEY", CACHED_KEY_PAIR.getPublic().getEncoded());

    static {
        ensureSchema();
    }

    private TestHelper() {
    }

    /**
     * jOOQ の {@link DSLContext} を返します。
     *
     * @return DSLContext
     */
    public static DSLContext dsl() {
        return DSL;
    }

    /**
     * 全テーブルのデータと Redis のキーを全て削除します。テストの前処理として呼び出します。
     */
    public static void truncateTables() {
        DSL.execute("SET FOREIGN_KEY_CHECKS=0");
        for (String table : new String[] {"jwt_histories", "notifications", "invitations", "clients", "staffs"}) {
            DSL.execute("TRUNCATE TABLE " + table);
        }
        DSL.execute("SET FOREIGN_KEY_CHECKS=1");
        try (var jedis = JEDIS_POOL.getResource()) {
            jedis.flushDB();
        }
    }

    /**
     * テスト用スタッフを1件作成します。
     *
     * @param email メールアドレス（省略時はランダムな一意値）
     * @param role  ロール（省略時は1=管理者）
     * @return 作成したスタッフ行
     */
    public static StaffRow createStaff(String email, int role) {
        LocalDateTime now = LocalDateTime.now();
        long id = DSL.insertInto(STAFFS)
                .set(STAFFS.NAME, "テストスタッフ")
                .set(STAFFS.EMAIL, email)
                .set(STAFFS.PROVIDER, 1)
                .set(STAFFS.PROVIDER_ID, "test-" + shortId())
                .set(STAFFS.ROLE, (long) role)
                .set(STAFFS.LAST_LOGIN_AT, now)
                .set(STAFFS.CREATED_AT, now)
                .set(STAFFS.CREATED_BY, 0L)
                .set(STAFFS.UPDATED_AT, now)
                .set(STAFFS.UPDATED_BY, 0L)
                .set(STAFFS.VERSION, 1L)
                .returning(STAFFS.ID)
                .fetchOne(STAFFS.ID);
        return new StaffRow(id, email);
    }

    /**
     * テスト用スタッフを1件作成します（メールアドレス・ロールは既定値）。
     *
     * @return 作成したスタッフ行
     */
    public static StaffRow createStaff() {
        return createStaff("staff-" + shortId() + "@example.com", 1);
    }

    /**
     * テスト用クライアントを1件作成します。
     *
     * @return 作成したクライアント行
     */
    public static ClientRow createClient() {
        LocalDateTime now = LocalDateTime.now();
        String identifier = "test-client-" + shortId();
        String accessToken = shortId() + shortId();
        long id = DSL.insertInto(CLIENTS)
                .set(CLIENTS.NAME, "テストクライアント")
                .set(CLIENTS.IDENTIFIER, identifier)
                .set(CLIENTS.POST_CODE, "100-0001")
                .set(CLIENTS.PREF, "東京都")
                .set(CLIENTS.CITY, "千代田区")
                .set(CLIENTS.ADDRESS, "千代田1-1")
                .set(CLIENTS.BUILDING, "")
                .set(CLIENTS.TEL, "0312345678")
                .set(CLIENTS.EMAIL, "client-" + shortId() + "@example.com")
                .set(CLIENTS.ACCESS_TOKEN, accessToken)
                .set(CLIENTS.PRIVATE_KEY, CACHED_PRIVATE_KEY_PEM)
                .set(CLIENTS.PUBLIC_KEY, CACHED_PUBLIC_KEY_PEM)
                .set(CLIENTS.FINGERPRINT, "SHA256:test")
                .set(CLIENTS.STATUS, 2L) // ClientStatus.Active
                .set(CLIENTS.CREATED_AT, now)
                .set(CLIENTS.CREATED_BY, 0L)
                .set(CLIENTS.UPDATED_AT, now)
                .set(CLIENTS.UPDATED_BY, 0L)
                .set(CLIENTS.VERSION, 1L)
                .returning(CLIENTS.ID)
                .fetchOne(CLIENTS.ID);
        return new ClientRow(id, identifier, accessToken, 1);
    }

    /**
     * テスト用招待を1件作成します。
     *
     * @param token 招待トークン（省略時はランダムな一意値）
     * @param role  ロール（省略時は2=メンバー）
     * @return 作成した招待行
     */
    public static InvitationRow createInvitation(String token, int role) {
        LocalDateTime now = LocalDateTime.now();
        int id = DSL.insertInto(INVITATIONS)
                .set(INVITATIONS.TOKEN, token)
                .set(INVITATIONS.ROLE, (long) role)
                .set(INVITATIONS.CREATED_AT, now)
                .set(INVITATIONS.CREATED_BY, 0L)
                .set(INVITATIONS.UPDATED_AT, now)
                .set(INVITATIONS.UPDATED_BY, 0L)
                .set(INVITATIONS.VERSION, 1L)
                .returning(INVITATIONS.ID)
                .fetchOne(INVITATIONS.ID)
                .intValue();
        return new InvitationRow(id, token);
    }

    /**
     * テスト用招待を1件作成します（トークン・ロールは既定値）。
     *
     * @return 作成した招待行
     */
    public static InvitationRow createInvitation() {
        return createInvitation(shortId() + shortId() + shortId() + shortId(), 2);
    }

    /**
     * テスト用通知を1件作成します。
     *
     * @param staffId 通知先スタッフID
     * @param title   タイトル
     * @param read    既読かどうか（省略時は未読）
     * @return 作成した通知行
     */
    public static NotificationRow createNotification(long staffId, String title, boolean read) {
        LocalDateTime now = LocalDateTime.now();
        long id = DSL.insertInto(NOTIFICATIONS)
                .set(NOTIFICATIONS.STAFF_ID, staffId)
                .set(NOTIFICATIONS.MESSAGE_TYPE, 1L)
                .set(NOTIFICATIONS.TITLE, title)
                .set(NOTIFICATIONS.MESSAGE, "テスト通知本文")
                .set(NOTIFICATIONS.READ, (short) (read ? 1 : 0))
                .set(NOTIFICATIONS.CREATED_AT, now)
                .set(NOTIFICATIONS.CREATED_BY, 0L)
                .set(NOTIFICATIONS.UPDATED_AT, now)
                .set(NOTIFICATIONS.UPDATED_BY, 0L)
                .set(NOTIFICATIONS.VERSION, 1L)
                .returning(NOTIFICATIONS.ID)
                .fetchOne(NOTIFICATIONS.ID);
        return new NotificationRow(id, staffId);
    }

    /**
     * テスト用通知を1件作成します（未読）。
     *
     * @param staffId 通知先スタッフID
     * @param title   タイトル
     * @return 作成した通知行
     */
    public static NotificationRow createNotification(long staffId, String title) {
        return createNotification(staffId, title, false);
    }

    /**
     * ランダムな短い一意文字列を返します（メール・トークン等の一意性確保用）。
     *
     * @return 8文字の16進文字列
     */
    private static String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * テスト用の2048bit RSA鍵ペアを生成します。
     *
     * @return 鍵ペア
     */
    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("failed to generate test RSA key pair", e);
        }
    }

    /**
     * DER エンコード済みの鍵バイト列を PEM 形式に変換します。
     *
     * @param label PEMヘッダー・フッターに使うラベル（"PRIVATE KEY" 等）
     * @param der   DERエンコード済みバイト列
     * @return PEM文字列
     */
    private static String toPem(String label, byte[] der) {
        String body = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der);
        return "-----BEGIN " + label + "-----\n" + body + "\n-----END " + label + "-----\n";
    }

    /**
     * jOOQ コード生成用のDDL（{@code schema.sql}）を使ってテスト用DBにテーブルを作成します
     * （存在しない場合のみ）。PHPのマイグレーションに依存せず、このヘルパー単独で
     * テストDBのセットアップを完結させます。
     */
    private static void ensureSchema() {
        String sql;
        try {
            sql = Files.readString(Path.of("src/main/resources/db/schema.sql"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // 行頭コメント（-- ...）を先に取り除いてから ; で分割する。コメント行がSQL文の
        // 直前に連続している場合、コメント行を含むチャンク全体が「--」で始まると
        // 誤判定され、後続の実SQL（例: 先頭の CREATE TABLE clients）が丸ごと
        // スキップされてしまうため。
        String withoutComments = sql.lines()
                .filter(line -> !line.strip().startsWith("--"))
                .reduce("", (acc, line) -> acc + line + "\n");
        for (String statement : withoutComments.split(";")) {
            String trimmed = statement.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            String idempotent = trimmed.replaceFirst("(?i)^CREATE TABLE ", "CREATE TABLE IF NOT EXISTS ");
            DSL.execute(idempotent);
        }
    }
}
