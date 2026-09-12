/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.http.responses;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 基底Responseクラスです（PHP版 App\Support\Http\Responses\AbstractResponse 相当）。
 * 派生クラスのフィールドを1件ずつMapへ書き出す手書きコードを避けるため、
 * リフレクションでフィールド名をsnake_caseに変換して {@link #attributes()} を組み立てます。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
public abstract class AbstractResponse {

    /**
     * 出力から除外するフィールド名を返します。既定では除外しません。
     *
     * @return 除外するフィールド名の集合
     */
    protected Set<String> getExcludeKeys() {
        return Set.of();
    }

    /**
     * 派生クラスの全フィールドをsnake_caseキーのMapへ変換します。
     *
     * @return JSON化用のMap
     */
    public Map<String, Object> attributes() {
        Set<String> excludes = getExcludeKeys();
        Map<String, Object> result = new LinkedHashMap<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (excludes.contains(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            try {
                result.put(toSnakeCase(field.getName()), field.get(this));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("failed to read response field: " + field.getName(), e);
            }
        }
        return result;
    }

    /**
     * キャメルケースの文字列をsnake_caseへ変換します。
     *
     * @param name キャメルケース文字列
     * @return snake_case文字列
     */
    private static String toSnakeCase(String name) {
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
