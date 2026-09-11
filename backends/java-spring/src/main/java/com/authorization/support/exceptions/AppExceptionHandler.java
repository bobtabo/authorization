/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.exceptions;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * {@link AppException} を HTTP レスポンスへ変換する一元例外ハンドラーです。
 * PHP版の {@code response()->failure()} と同じ {@code {"message": ...}} 形式で返します。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@RestControllerAdvice
public class AppExceptionHandler {

    /**
     * {@link AppException} を捕捉し、ステータスコードとメッセージを JSON へ変換します。
     *
     * @param e アプリケーション例外
     * @return エラーレスポンス
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getMessage()));
    }
}
