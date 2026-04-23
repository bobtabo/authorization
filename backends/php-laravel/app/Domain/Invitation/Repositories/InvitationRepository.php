<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Domain\Invitation\Repositories;

use App\Domain\Invitation\Condition\InvitationCondition;
use App\Domain\Invitation\Entities\Invitation;

/**
 * 招待Repositoryのインターフェースです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Invitation\Repositories
 */
interface InvitationRepository
{
    /**
     * 現在の招待情報を取得します。
     *
     * @return Invitation|null 未設定時は null
     */
    public function getCurrent(): ?Invitation;

    /**
     * 招待を更新します。
     *
     * @param Invitation $entity 招待エンティティ
     * @return Invitation 更新された招待
     */
    public function persist(Invitation $entity): Invitation;

    /**
     * トークンから招待情報を解決します（未登録・不正なら null）。
     *
     * @param InvitationCondition $condition 検索条件
     * @return Invitation|null 該当がなければ null
     */
    public function findByToken(InvitationCondition $condition): ?Invitation;
}
