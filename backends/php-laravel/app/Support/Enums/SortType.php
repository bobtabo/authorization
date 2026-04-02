<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Enums;

/**
 * ソート種別Enumクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
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
