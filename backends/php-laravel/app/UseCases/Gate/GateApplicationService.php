<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Gate;

use App\Domain\Gate\JwtIssuerInterface;
use App\Domain\Gate\JwtVerifierInterface;
use App\Support\Services\AbstractService;

/**
 * JWT の発行・検証のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Gate
 */
class GateApplicationService extends AbstractService
{
    /**
     * @param  JwtIssuerInterface  $issuer  JWT 発行ポート
     * @param  JwtVerifierInterface  $verifier  JWT 検証ポート
     */
    public function __construct(
        private readonly JwtIssuerInterface $issuer,
        private readonly JwtVerifierInterface $verifier,
    ) {}

    /**
     * 会員ID に紐づく JWT 発行結果を取得します。
     *
     * @param  string  $memberId  クライアント会員ID
     * @return array{message: string} 発行処理結果
     */
    public function issueToken(string $memberId): array
    {
        return $this->issuer->issueForMember($memberId);
    }

    /**
     * JWT を検証しクレームを返します。
     *
     * @param  string  $identifier  クライアント識別名
     * @param  string  $token  JWT 文字列
     * @return array<string, mixed> 検証結果（Payload 相当）
     */
    public function verify(string $identifier, string $token): array
    {
        return $this->verifier->verify($identifier, $token);
    }
}
