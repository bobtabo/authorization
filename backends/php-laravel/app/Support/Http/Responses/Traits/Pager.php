<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Http\Responses\Traits;

use App\Support\Enums\SortType;
use App\Support\Traits\Assign;

/**
 * ページャーTraitです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
 * @package App\Support\Http\Responses\Traits
 */
trait Pager
{
    protected int $count = 0;
    protected int $offset = 0;
    protected int $limit = 0;
    protected string $sort = '';
    protected SortType $sortType = SortType::NONE;
    protected string $sortTypeValue = '';

    /**
     * ページャーを作成します。
     *
     * @param int $recordCount データ件数
     * @return mixed ページャー
     */
    public function createPager(int $recordCount): mixed
    {
        $defaultPageCount = $this->getDefaultPageCount();
        $page = $this->getPage();
        $pageCount = $this->getPageCount();
        $value = [
            'count' => $this->count,
            'limit' => $this->limit,
            'next' => $this->hasNextPage(),
            'previous' => $this->hasPreviousPage(),
            'page' => $page,
            'nextPage' => $page + 1,
            'previousPage' => $page - 1,
            'pageCount' => $pageCount,
            'first' => ($page > 1),
            'last' => ($pageCount > $page),
            'firstRecordCount' => $this->getOffset() + 1,
            'lastRecordCount' => $this->getOffset() + $recordCount,
            'startPage' => $this->getStartPage($defaultPageCount),
            'endPage' => $this->getEndPage($defaultPageCount),
        ];
        $this->sortTypeValue = $this->sortType->value;

        return new class ($value) {
            use Assign;

            public int $count = 0;
            public int $limit = 0;
            public bool $next = false;
            public bool $previous = false;
            public int $page = 0;
            public int $nextPage = 0;
            public int $previousPage = 0;
            public int $pageCount = 0;
            public bool $first = false;
            public bool $last = false;
            public int $firstRecordCount = 0;
            public int $lastRecordCount = 0;
            public int $startPage = 0;
            public int $endPage = 0;

            public function __construct(array $value)
            {
                $this->assign($value);
            }
        };
    }

    /**
     * オフセットを取得します。
     *
     * @return int オフセット
     */
    protected function getOffset(): int
    {
        if (!empty($this->count)) {
            if ($this->offset > $this->getLastPageOffset()) {
                return $this->getLastPageOffset();
            }
        }
        return $this->offset;
    }

    /**
     * 次頁があるか確認します。
     *
     * @return bool 次頁がある場合 true を返します
     */
    protected function hasNextPage(): bool
    {
        return $this->getPageCount() > $this->getPage();
    }

    /**
     * 前頁があるか確認します。
     *
     * @return bool 前頁がある場合 true を返します
     */
    protected function hasPreviousPage(): bool
    {
        return $this->getPage() > 1;
    }

    /**
     * 頁を取得します。
     *
     * @return int 頁
     */
    protected function getPage(): int
    {
        if (empty($this->limit)) {
            return 1;
        }
        return floor(ceil($this->getOffset() / $this->limit) + 1);
    }

    /**
     * 開始頁を取得します。
     *
     * @param int $defaultPageCount デフォルト頁数
     * @return int 開始頁
     */
    protected function getStartPage(int $defaultPageCount): int
    {
        $start = $this->getPage() - ($defaultPageCount - 1);
        if ($start <= 0) {
            $start = 1;
        }
        return $start;
    }

    /**
     * 最終頁を取得します。
     *
     * @param int $defaultPageCount デフォルト頁数
     * @return int 最終頁
     */
    protected function getEndPage(int $defaultPageCount): int
    {
        $end = $this->getStartPage($defaultPageCount) + ($defaultPageCount - 1);
        $pageCount = $this->getPageCount();
        if ($end > $pageCount) {
            $end = $pageCount;
        }
        return $end;
    }

    /**
     * 頁数を取得します。
     *
     * @return int 頁数
     */
    protected function getPageCount(): int
    {
        if (empty($this->limit)) {
            return 1;
        }

        $result = ceil($this->count / $this->limit);
        if ($result < 1) {
            $result = 1;
        }

        return $result;
    }

    /**
     * 最終頁オフセットを取得します。
     *
     * @return int 最終頁オフセット
     */
    protected function getLastPageOffset(): int
    {
        return ($this->getPageCount() * $this->limit) - $this->limit;
    }

    /**
     * デフォルト頁数を取得します。
     *
     * @return int デフォルト頁数
     */
    protected function getDefaultPageCount(): int
    {
        return config('authorization.app.page.count');
    }
}
