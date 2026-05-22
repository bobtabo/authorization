<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\JwtHistory;

use App\Domain\JwtHistory\Condition\JwtHistoryCondition;
use App\Domain\JwtHistory\Repositories\JwtHistoryRepository;
use App\Domain\JwtHistory\ValueObjects\JwtHistoryListVo;
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
        $condition = new JwtHistoryCondition();
        $condition->clientId = $dto->clientId;

        $list = $this->repository->findByClientId($condition);

        $vo = new JwtHistoryListVo();
        $vo->assignItems($list);

        return $vo;
    }
}
