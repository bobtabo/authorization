<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Dtos;

use App\Support\Enums\SortType;

/**
 * ページングDTOクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Dtos
 */
class PagerDto extends AbstractDto
{
    public int $offset = 0;
    public int $limit = 0;
    public string $sort = '';
    public SortType $sortType = SortType::NONE;

    /**
     * {@inheritdoc}
     */
    public function assign(array $values, array $convert = [], array $excludes = []): mixed
    {
        parent::assign($values, $convert, $excludes);

        $page = array_key_exists('page', $values) ? $values['page'] : config('authorization.app.pager.current');
        $this->setPaging(intval($page));

        return $this;
    }

    /**
     * ページング設定します。
     *
     * @param int $page ページ番号
     * @return void
     */
    public function setPaging(int $page): void
    {
        if (empty($this->limit)) {
            $this->limit = config('authorization.app.pager.limit');
        }

        $this->offset = $this->limit * ($page - 1);
    }
}
