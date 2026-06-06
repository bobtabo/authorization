<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Staff\ValueObjects;

use App\Domain\Staff\Entities\Staff;
use App\Domain\Staff\Mappers\StaffApiMapper;
use App\Support\Enums\SortType;
use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;
use Illuminate\Support\Collection;

/**
 * スタッフ一覧の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Staff\ValueObjects
 */
class StaffListVo extends AbstractValueObject
{
    use Getter;

    protected Collection $items;
    private int $count = 0;
    private int $offset = 0;
    private int $limit = 0;
    private string $sort = '';
    private SortType $sortType = SortType::NONE;

    /**
     * スタッフ Entity のリストを一覧行に設定します。
     *
     * @param  iterable<int, Staff>  $list
     */
    public function assignStaff(iterable $list): void
    {
        $this->items = collect();
        foreach ($list as $entity) {
            $row = new StaffResourceVo;
            $row->assign(array_merge(['found' => true], StaffApiMapper::toListItem($entity)));
            $this->items->push($row);
        }
    }

    /**
     * 総件数を設定します。
     *
     * @param int $count 総件数
     */
    public function setCount(int $count): void
    {
        $this->count = $count;
    }

    /**
     * ページング情報を設定します。
     *
     * @param int $offset オフセット
     * @param int $limit 取得件数
     * @param string $sort ソート対象
     * @param SortType $sortType ソート順
     */
    public function setPaging(int $offset, int $limit, string $sort, SortType $sortType): void
    {
        $this->offset = $offset;
        $this->limit = $limit;
        $this->sort = $sort;
        $this->sortType = $sortType;
    }

    /**
     * {@inheritdoc}
     *
     * @return array<string, mixed>
     */
    #[\Override]
    public function attributes(): array
    {
        return [
            'items' => $this->items
                ->map(static fn (StaffResourceVo $row): array => $row->attributes())
                ->values()
                ->all(),
            'count' => $this->count,
            'offset' => $this->offset,
            'limit' => $this->limit,
            'sort' => $this->sort,
            'sortType' => $this->sortType,
        ];
    }
}
