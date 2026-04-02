<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Database;

use App;
use DB;
use File;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Seeder as BaseSeeder;
use SplFileObject;
use Str;

/**
 * データファイルを登録する拡張Seederクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Database
 */
class Seeder extends BaseSeeder
{
    /**
     * @var string|null 実行するファイル名
     */
    protected ?string $target = null;

    /**
     * Seed the application's database.
     *
     * @return void
     */
    public function run()
    {
        $this->execute('data');

        if (App::isProduction()) {
            $this->execute('data/production');
        }

        if (App::isStaging()) {
            $this->execute('data/staging');
        }

        if (App::isDevelop()) {
            $this->execute('data/develop');
        }

        if (App::isLocal()) {
            $this->execute('data/local');
        }
    }

    /**
     * データを登録します。
     *
     * @param string $dir ディレクトリパス
     * @return void
     */
    private function execute(string $dir): void
    {
        Model::unguard();

        foreach (glob('database/' . $dir . '/*') as $file) {
            if (!is_file($file)) {
                continue;
            }

            $extention = File::extension($file);

            if (!empty($this->target)) {
                if (!Str::contains($file, $this->target . '.')) {
                    continue;
                }
            }

            if ($extention === 'csv') {
                $this->executeCsv($file);
            } elseif ($extention === 'sql') {
                $this->executeSql($file);
            }
        }

        Model::reguard();
    }

    /**
     * CSVデータを登録します。
     *
     * @param string $path ファイルパス
     * @return void
     */
    private function executeCsv(string $path): void
    {
        $table = basename($path, ".csv");
        if (Str::contains($table, '-')) {
            $list = $tables = Str::of($table)->explode('-');
            $table = $list->last();
        }

        $file = new SplFileObject($path);
        $file->setFlags(
            \SplFileObject::READ_CSV |
            \SplFileObject::READ_AHEAD |
            \SplFileObject::SKIP_EMPTY |
            \SplFileObject::DROP_NEW_LINE
        );

        DB::table($table)->delete();

        $list = [];
        $column = [];
        $lineFix = [];
        foreach ($file as $line) {
            if (empty($column)) {
                $column = $line;
            } else {
                $lineFix = array_merge($line, $lineFix);
                if (count($column) == count($lineFix)) {
                    $list[] = $this->getRow($column, $lineFix);
                    $lineFix = [];
                }
            }
        }

        //1000件ずつ登録します
        foreach (array_chunk($list, 1000) as $chunk) {
            DB::table($table)->insert($chunk);
        }
    }

    /**
     * SQLデータを登録します。
     *
     * @param string $path ファイルパス
     * @return void
     */
    private function executeSql(string $path): void
    {
        $table = basename($path, ".sql");
        if (Str::contains($table, '-')) {
            $list = Str::of($table)->explode('-');
            $table = $list->last();
        }

        $file = new SplFileObject($path);
        $file->setFlags(
            \SplFileObject::READ_AHEAD |
            \SplFileObject::SKIP_EMPTY |
            \SplFileObject::DROP_NEW_LINE
        );

        DB::table($table)->delete();

        foreach ($file as $line) {
            $sql = Str::replaceLast(';', '', $line);
            if (empty(trim($sql))) {
                continue;
            }
            DB::insert($sql);
        }
    }

    /**
     * テーブル行を取得します。
     *
     * @param string[] $columns CSV列配列
     * @param string[] $line CSV行配列
     * @return array<string, string> テーブル行配列
     */
    private function getRow(array $columns, array $line) : array
    {
        $result = [];

        $i = 0;
        foreach ($columns as $column) {
            if ($line[$i] === '_NULL_') {
                $result[$column] = null;
            } else {
                $result[$column] = $line[$i];
            }
            $i++;
        }

        return $result;
    }
}
