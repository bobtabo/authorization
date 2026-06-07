<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\JwtHistory;

use App\Domain\Client\Condition\JwtHistoryCondition;
use App\Domain\Client\Repositories\JwtHistoryRepository;
use App\Domain\Client\ValueObjects\JwtHistoryListVo;
use App\Support\Enums\SortType;
use App\Support\Repositories\Conditions\Option;
use App\Support\Services\AbstractService;
use App\UseCases\JwtHistory\Dtos\JwtHistoryDto;

/**
 * JWT履歴Serviceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\JwtHistory
 */
class JwtHistoryService extends AbstractService
{
    /**
     * コンストラクタ。
     *
     * @param JwtHistoryRepository $repository JWT履歴Repository
     */
    public function __construct(
        private readonly JwtHistoryRepository $repository,
    ) {
    }

    /**
     * クライアントに紐づくJWT履歴一覧を返します。
     *
     * @param JwtHistoryDto $dto JWT履歴DTO
     * @return JwtHistoryListVo JWT履歴一覧ValueObject
     */
    public function getHistories(JwtHistoryDto $dto): JwtHistoryListVo
    {
        if (empty($dto->sort)) {
            $dto->sort = 'issue_at';
        }
        if ($dto->sortType === SortType::NONE) {
            $dto->sortType = SortType::DESC;
        }

        $condition = new JwtHistoryCondition();
        $condition->clientId = $dto->clientId;
        $condition->option = new Option($dto->offset, $dto->limit, $dto->sort, $dto->sortType);

        $count = $this->repository->countByClientId($condition);
        $list = $this->repository->findByClientId($condition);

        $vo = new JwtHistoryListVo();
        $vo->assignItems($list);
        $vo->setCount($count);
        $vo->setPaging($dto->offset, $dto->limit, $dto->sort, $dto->sortType);

        return $vo;
    }
}
