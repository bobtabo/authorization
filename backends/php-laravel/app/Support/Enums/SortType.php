<?php
/**
 * This is a program developed by Strategic Insights, Inc.
 *
 * Copyright (c) Strategic Insights, Inc. All Rights Reserved.
 */
namespace App\Support\Enums;

/**
 * ソート種別Enumクラスです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
 * @package App\Support\Enums
 */
enum SortType: string
{
    use Values;

    case NONE = '';
    case ASC = 'ASC';
    case DESC = 'DESC';

    /**
     * 説明を取得します。
     *
     * @return string 説明
     */
    public function description(): string
    {
        return match ($this) {
            self::NONE => 'なし',
            self::ASC => '昇順',
            self::DESC => '降順',
        };
    }
}
