<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\Condition;

use App\Domain\Client\Enums\ClientStatus;
use App\Support\Repositories\Conditions\AbstractCondition;
use Carbon\Carbon;

/**
 * クライアントConditionクラスです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
 * @package App\Domain\Client\Condition
 */
class ClientCondition extends AbstractCondition
{    public ?string $name = null;
    public ?string $identifer = null;
    public ?string $postCode = null;
    public ?string $pref = null;
    public ?string $city = null;
    public ?string $address = null;
    public ?string $building = null;
    public ?string $tel = null;
    public ?string $email = null;
    public ?string $accessToken = null;
    public ?string $privateKey = null;
    public ?string $publicKey = null;
    public ?string $fingerprint = null;
    public ?ClientStatus $status = null;
    public ?Carbon $startAt = null;
    public ?Carbon $stopAt = null;

    /**
     * 一覧検索用キーワード（名前・識別子の部分一致）。API の keyword からマッピングします。
     */
    public ?string $keyword = null;

    /**
     * 利用開始日 From（Y-m-d 相当、空は無条件）。
     */
    public ?Carbon $startFrom = null;

    /**
     * 利用開始日 To（Y-m-d 相当、空は無条件）。
     */
    public ?Carbon $startTo = null;

    /**
     * 状態コードの一覧（空は無条件）。API の status クエリを正規化して設定します。
     *
     * @var array<int, int>
     */
    public array $statuses = [];
}
