/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.http.responses;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * PHP版の {@code Response::macro('success', ...)} 相当のレスポンス組み立てヘルパーです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public final class ResponseHelper {

    /**
     * {@code {"message": "SUCCESS", ...data}} 形式で 200 応答を返します。
     *
     * @param data 追加データ
     * @return レスポンス
     */
    public static ResponseEntity<Map<String, Object>> success(Map<String, Object> data) {
        return success(data, HttpStatus.OK.value());
    }

    /**
     * {@code {"message": "SUCCESS", ...data}} 形式で応答を返します。
     *
     * @param data 追加データ
     * @param status HTTPステータスコード
     * @return レスポンス
     */
    public static ResponseEntity<Map<String, Object>> success(Map<String, Object> data, int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "SUCCESS");
        body.putAll(data);
        return ResponseEntity.status(status).body(body);
    }

    /**
     * message envelope を付けず、そのままの内容で 200 応答を返します
     * （クライアント一覧・JWT履歴一覧など、PHP版で {@code response()->json()} を使う箇所向け）。
     *
     * @param data レスポンス本体
     * @return レスポンス
     */
    public static ResponseEntity<Map<String, Object>> json(Map<String, Object> data) {
        return ResponseEntity.ok(data);
    }

    /**
     * ユーティリティクラスのためインスタンス化を禁止します。
     */
    private ResponseHelper() {
    }
}
