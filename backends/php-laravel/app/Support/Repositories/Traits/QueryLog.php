<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Repositories\Traits;

use Carbon\Carbon;
use DateTime;
use DB;
use Illuminate\Database\Eloquent\Builder;
use Log;
use Str;

/**
 * 実行クエリーのデバッグログ出力Traitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories\Traits
 */
trait QueryLog
{
    /**
     * クエリーログを開始します。
     *
     * @return void
     */
    protected function beginQueryLog(): void
    {
        if (!app()->isLocal()) {
            return;
        }

        DB::enableQueryLog();
    }

    /**
     * クエリーログを終了します。
     *
     * @return void
     */
    protected function closeQueryLog(): void
    {
        if (!app()->isLocal()) {
            return;
        }

        $this->saveQueryLog();

        DB::flushQueryLog();
        DB::disableQueryLog();
    }

    /**
     * クエリーログを開始→出力→終了します。
     *
     * @param Builder $query クエリー
     * @return void
     */
    protected function runQueryLog(Builder $query): void
    {
        $this->beginQueryLog();
        $query->get();
        $this->closeQueryLog();
    }

    /**
     * クエリーログを登録します。
     *
     * @return void
     */
    private function saveQueryLog(): void
    {
        $sqls = DB::getQueryLog();

        if (empty($sqls)) {
            return;
        }

        $exclude = [
            'BEGIN',
            'LOOP',
            'all_tables',
            'migration',
            'create table',
            'create unique',
            'create index',
            'create sequence',
            'comment on',
            'query.insertOne'
        ];

        foreach ($sqls as $sql) {
            if (Str::contains($sql['query'], $exclude, true)) {
                continue;
            }

            $query = $sql['query'];
            $bindings = [];
            if (!empty($sql['bindings'])) {
                foreach ($sql['bindings'] as $binding) {
                    if (is_string($binding)) {
                        $bindings[] = "'$binding'";
                    } elseif (is_bool($binding)) {
                        $bindings[] = $binding ? '1' : '0';
                    } elseif (is_int($binding)) {
                        $bindings[] = (string)$binding;
                    } elseif ($binding === null) {
                        $bindings[] = 'NULL';
                    } elseif ($binding instanceof Carbon) {
                        $bindings[] = "'{$binding->toDateTimeString()}'";
                    } elseif ($binding instanceof DateTime) {
                        $bindings[] = "'{$binding->format('Y-m-d H:i:s')}'";
                    }
                }
            }

            if (!empty($bindings)) {
                $query = Str::replaceArray('?', $bindings, $query);
            }

            //デバッグログ出力します
            Log::debug($sql['time'] / 1000 . ':' . $query);
        }
    }
}
