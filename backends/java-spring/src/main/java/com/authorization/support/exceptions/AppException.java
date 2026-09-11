/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.exceptions;

/**
 * アプリケーションExceptionクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public class AppException extends RuntimeException {

    private final int statusCode;

    /**
     * コンストラクタ。
     *
     * @param statusCode HTTPステータスコード
     * @param message メッセージキー
     */
    public AppException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /**
     * 不正リクエスト例外を取得します。
     *
     * @param message エラーメッセージキー
     * @return 例外
     */
    public static AppException badRequest(String message) {
        return new AppException(400, message);
    }

    /**
     * 認証エラー例外を取得します。
     *
     * @param message エラーメッセージキー
     * @return 例外
     */
    public static AppException unauthorized(String message) {
        return new AppException(401, message);
    }

    /**
     * アクセス拒否の例外を取得します。
     *
     * @param message エラーメッセージキー
     * @return 例外
     */
    public static AppException forbidden(String message) {
        return new AppException(403, message);
    }

    /**
     * リソース未検出の例外を取得します。
     *
     * @param message エラーメッセージキー
     * @return 例外
     */
    public static AppException notFound(String message) {
        return new AppException(404, message);
    }

    /**
     * リクエスト制限の例外を取得します。
     *
     * @param message エラーメッセージキー
     * @return 例外
     */
    public static AppException manyRequest(String message) {
        return new AppException(429, message);
    }

    /**
     * サーバー内部エラー例外を取得します。
     *
     * @param message エラーメッセージキー
     * @return 例外
     */
    public static AppException internal(String message) {
        return new AppException(500, message);
    }

    /**
     * サービス利用不可の例外を取得します。
     *
     * @param message エラーメッセージキー
     * @return 例外
     */
    public static AppException unavailable(String message) {
        return new AppException(503, message);
    }
}
