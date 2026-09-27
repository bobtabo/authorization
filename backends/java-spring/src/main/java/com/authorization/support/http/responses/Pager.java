/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.support.http.responses;

import lombok.Getter;

/**
 * ページャー計算クラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Getter
public class Pager {

    /** ページ番号の表示数（{@code config/authorization/app.php} の {@code pager.page} 相当）。 */
    private static final int DEFAULT_PAGE_COUNT = 5;

    private final int count;
    private final int limit;
    private final boolean next;
    private final boolean previous;
    private final int page;
    private final int nextPage;
    private final int previousPage;
    private final int pageCount;
    private final boolean first;
    private final boolean last;
    private final int firstRecordCount;
    private final int lastRecordCount;
    private final int startPage;
    private final int endPage;

    /**
     * ページャーを計算します。
     *
     * @param count 総件数
     * @param offset オフセット
     * @param limit 取得件数
     * @param recordCount 現在ページの実件数
     */
    public Pager(int count, int offset, int limit, int recordCount) {
        this.count = count;
        this.limit = limit;

        int pageCountValue = pageCount(count, limit);
        int effectiveOffset = effectiveOffset(count, offset, limit, pageCountValue);
        int pageValue = page(effectiveOffset, limit);
        int startPageValue = startPage(pageValue);
        int endPageValue = endPage(startPageValue, pageCountValue);

        this.pageCount = pageCountValue;
        this.page = pageValue;
        this.next = pageCountValue > pageValue;
        this.previous = pageValue > 1;
        this.nextPage = pageValue + 1;
        this.previousPage = pageValue - 1;
        this.first = pageValue > 1;
        this.last = pageCountValue > pageValue;
        this.firstRecordCount = effectiveOffset + 1;
        this.lastRecordCount = effectiveOffset + recordCount;
        this.startPage = startPageValue;
        this.endPage = endPageValue;
    }

    /**
     * 総件数と limit から総ページ数を求めます。
     *
     * @param count 総件数
     * @param limit 取得件数
     * @return 総ページ数（最低 1）
     */
    private static int pageCount(int count, int limit) {
        if (limit <= 0) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil((double) count / limit));
    }

    /**
     * オフセットが最終ページを超える場合、最終ページのオフセットへ丸めます。
     *
     * @param count 総件数
     * @param offset オフセット
     * @param limit 取得件数
     * @param pageCount 総ページ数
     * @return 丸め後のオフセット
     */
    private static int effectiveOffset(int count, int offset, int limit, int pageCount) {
        if (count <= 0) {
            return offset;
        }
        int lastPageOffset = (pageCount * limit) - limit;
        return offset > lastPageOffset ? lastPageOffset : offset;
    }

    /**
     * オフセットと limit から現在ページ番号を求めます。
     *
     * @param offset オフセット
     * @param limit 取得件数
     * @return 現在ページ番号
     */
    private static int page(int offset, int limit) {
        if (limit <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) offset / limit) + 1;
    }

    /**
     * ページャー表示の開始ページを求めます。
     *
     * @param page 現在ページ番号
     * @return 開始ページ
     */
    private static int startPage(int page) {
        int start = page - (DEFAULT_PAGE_COUNT - 1);
        return start <= 0 ? 1 : start;
    }

    /**
     * ページャー表示の終了ページを求めます。
     *
     * @param startPage 開始ページ
     * @param pageCount 総ページ数
     * @return 終了ページ
     */
    private static int endPage(int startPage, int pageCount) {
        int end = startPage + (DEFAULT_PAGE_COUNT - 1);
        return end > pageCount ? pageCount : end;
    }
}
