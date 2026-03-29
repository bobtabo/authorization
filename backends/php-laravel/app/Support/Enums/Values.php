<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Enums;

use Arr;
use UnitEnum;

/**
 * Enum値Traitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package Sii\Selloop\Core\Enums\Traits
 */
trait Values
{
    /**
     * Enum値と内容の配列を取得します。
     *
     * @param UnitEnum[] $excludes 除外するCase
     * @param bool $useLabel labelを参照する場合 true を設定します
     * @return array Enum値と内容の配列
     */
    public static function values(array $excludes = [], bool $useLabel = false): array
    {
        $result = [];

        $cases = static::casesByOrder();
        if (empty($cases)) {
            $cases = static::cases();
        }

        foreach ($cases as $case) {
            $matches = Arr::where($excludes, function ($value) use ($case) {
                return $value === $case;
            });

            if (!empty($matches)) {
                continue;
            }

            $result[$case->value] = $useLabel ? $case->label() : $case->description();
        }

        return $result;
    }

    /**
     * 対象EnumをCASE名で取得します。
     *
     * @param string $name CASE名
     * @return UnitEnum|null Enum
     */
    public static function fromByName(string $name): ?UnitEnum
    {
        $result = null;
        foreach (static::cases() as $case) {
            if ($case->name == $name) {
                $result = $case;
                break;
            }
        }
        return $result;
    }

    /**
     * 対象Enumをラベル名で取得します。
     *
     * @param string $label ラベル名
     * @return UnitEnum|null Enum
     */
    public static function fromByLabel(string $label): ?UnitEnum
    {
        $result = null;
        foreach (static::cases() as $case) {
            if ($case->label() == $label) {
                $result = $case;
                break;
            }
        }
        return $result;
    }

    /**
     * 対象Enumを説明で取得します。
     *
     * @param string $name CASE名
     * @return UnitEnum|null Enum
     */
    public static function fromByDescription(string $name): ?UnitEnum
    {
        $result = null;
        foreach (static::cases() as $case) {
            if ($case->description() == $name) {
                $result = $case;
                break;
            }
        }
        return $result;
    }

    /**
     * CASE並び順を定義します。
     *
     * @return array CASE配列
     */
    protected static function casesByOrder(): array
    {
        return [];
    }
}
