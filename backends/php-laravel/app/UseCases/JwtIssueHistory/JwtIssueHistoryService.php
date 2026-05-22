<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\JwtIssueHistory;

use App\Domain\JwtIssueHistory\Condition\JwtIssueHistoryCondition;
use App\Domain\JwtIssueHistory\Repositories\JwtIssueHistoryRepository;
use App\Domain\JwtIssueHistory\ValueObjects\JwtIssueHistoryListVo;
use App\Support\Services\AbstractService;
use App\UseCases\JwtIssueHistory\Dtos\JwtIssueHistoryDto;

/**
 * JWT履歴Serviceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\JwtIssueHistory
 */
class JwtIssueHistoryService extends AbstractService
{
    /**
     * コンストラクタ。
     *
     * @param JwtIssueHistoryRepository $repository JWT履歴Repository
     */
    public function __construct(
        private readonly JwtIssueHistoryRepository $repository,
    ) {
    }

    /**
     * クライアントに紐づくJWT履歴一覧を返します。
     *
     * @param JwtIssueHistoryDto $dto JWT履歴DTO
     * @return JwtIssueHistoryListVo JWT履歴一覧ValueObject
     */
    public function getHistories(JwtIssueHistoryDto $dto): JwtIssueHistoryListVo
    {
        $condition = new JwtIssueHistoryCondition();
        $condition->clientId = $dto->clientId;

        $list = $this->repository->findByClientId($condition);

        $vo = new JwtIssueHistoryListVo();
        $vo->assignItems($list);

        return $vo;
    }
}
