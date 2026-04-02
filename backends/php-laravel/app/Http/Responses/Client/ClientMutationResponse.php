<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Http\Responses\Client;

use App\Support\Http\Responses\AbstractResponse;
use App\Support\Traits\Getter;
use Carbon\Carbon;

/**
 * クライアント登録・更新の HTTP レスポンス用オブジェクトです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Http\Responses\Client
 */
class ClientMutationResponse extends AbstractResponse
{
    use Getter;

    private ?int $id = null;
    private ?string $name = null;
    private ?string $identifier = null;
    private ?string $post_code = null;
    private ?string $pref = null;
    private ?string $city = null;
    private ?string $address = null;
    private ?string $building = null;
    private ?string $tel = null;
    private ?string $email = null;
    private ?int $status = null;
    private ?Carbon $startAtCarbon = null;
    private ?Carbon $stopAtCarbon = null;
    private ?Carbon $createdAtCarbon = null;
    private ?Carbon $updatedAtCarbon = null;
    private ?string $startAt = null;
    private ?string $stopAt = null;
    private ?string $createdAt = null;
    private ?string $updatedAt = null;

    public bool $ok = true;
    public string $message = 'SUCCESS';

    /**
     * @inheritdoc}
     */
    #[\Override]
    public function attributes(): array
    {
        $this->startAt = empty($this->startAtCarbon) ? '' : $this->startAtCarbon->toIso8601String();
        $this->stopAt = empty($this->stopAtCarbon) ? '' : $this->stopAtCarbon->toIso8601String();
        $this->createdAt = empty($this->createdAtCarbon) ? '' : $this->createdAtCarbon->toIso8601String();
        $this->updatedAt = empty($this->updatedAtCarbon) ? '' : $this->updatedAtCarbon->toIso8601String();

        return parent::attributes();
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getExcludeKeys(): array
    {
        return [
            'startAtCarbon',
            'stopAtCarbon',
            'createdAtCarbon',
            'updatedAtCarbon',
        ];
    }
}
