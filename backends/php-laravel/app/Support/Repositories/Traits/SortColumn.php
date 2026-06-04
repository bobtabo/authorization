<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Repositories\Traits;

/**
 * ソート列の設定Traitです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Repositories\Traits
 */
trait SortColumn
{
    /*
     * 使い方
     *
     * use App\Support\Repositories\Conditions\Option;
     *
     * class ExampleService extends AbstractService
     * {
     *     public function getExamples(ExampleDto $dto): ExampleValueObject
     *     {
     *         $condition = SimpleMapper::map($dto, ExampleCondition::class);
     *         $condition->option = new Option($dto->offset, $dto->limit, $dto->sort, $dto->sortType);
     *
     *         $list = $this->exampleRepository->getList($condition);
     *         ・・・
     *     }
     * }
     *
     * class ExampleRepository extends AbstractEloquentRepository
     * {
     *    use SortColumn;
     *
     *    public function getList(ExampleCondition $condition): Collection
     *    {
     *        $query = Model::query();
     *        ・・・
     *        // Orderby でサブクエリーを使用する場合、ソート列を追加します
     *        if ($condition->isOrderBy(static::$exampleName)) {
     *            $query->selectRaw($this->addSortColumn(static::$exampleName));
     *        }
     *
     *        if ($condition->isPaging()) {
     *            $query = $this->addOption($query, $condition->option);
     *        }
     *
     *        return $this->findByQuery($query, $condition);
     *    }
     * }
     */

    protected static string $exampleName = 'example_name';

    /**
     * ソート列を追加します。
     *
     * @param string $as エイリアス
     * @return string ソート列
     */
    protected function addSortColumn(string $as): string
    {
        return match ($as) {
            static::$exampleName => $this->getExampleNameColumn($as),
        };
    }

    /**
     * サンプル列を取得します。
     *
     * @param string $as エイリアス
     * @return string サンプル列
     */
    private function getExampleNameColumn(string $as): string
    {
        return '
            CONCAT(
                CONCAT(members.last_name, members.first_name),
                CONCAT(members.last_kana, members.first_kana)
            )
         as ' . $as;
    }
}
