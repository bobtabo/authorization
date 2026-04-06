<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Gate;

use App\Domain\Gate\JwtIssuerInterface;

/**
 * JWT 発行を仮実装するStubクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Gate
 * @deprecated 削除？
 */
class StubJwtIssuer implements JwtIssuerInterface
{
    /**
     * {@inheritdoc}
     *
     * @param  string  $memberId  クライアント会員ID（スタブでは未使用）
     * @return array{message: string} 成功メッセージのみ
     */
    #[\Override]
    public function issueForMember(string $memberId): array
    {
        return [
            'message' => 'SUCCESS',
        ];
    }
}
