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

    /**
     * 一覧行（{@see StaffResourceVo}）のコレクションです。
     */
    protected Collection $items;

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
     * {@inheritdoc}
     *
     * @return array{items: list<array<string, mixed>>}
     */
    #[\Override]
    public function attributes(): array
    {
        return [
            'items' => $this->items
                ->map(static fn (StaffResourceVo $row): array => $row->attributes())
                ->values()
                ->all(),
        ];
    }
}
