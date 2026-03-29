<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Gate;

use App\Domain\Gate\JwtIssuerInterface;
use App\Domain\Gate\JwtVerifierInterface;

/**
 * JWT の発行・検証のユースケースを提供するApplicationServiceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Gate
 */
final class GateApplicationService
{
    public function __construct(
        private readonly JwtIssuerInterface $issuer,
        private readonly JwtVerifierInterface $verifier,
    ) {}

    /**
     * @return array{message: string}
     */
    public function issueToken(string $memberId): array
    {
        return $this->issuer->issueForMember($memberId);
    }

    /**
     * @return array<string, mixed>
     */
    public function verify(string $identifier, string $token): array
    {
        return $this->verifier->verify($identifier, $token);
    }
}
