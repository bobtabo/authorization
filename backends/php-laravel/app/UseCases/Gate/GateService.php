<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Gate;

use App\Domain\Gate\JwtIssuerInterface;
use App\Domain\Gate\JwtVerifierInterface;
use App\Domain\Gate\ValueObjects\GateIssueVo;
use App\Domain\Gate\ValueObjects\GateVerifyVo;
use App\Support\Services\AbstractService;
use App\UseCases\Gate\Dtos\GateIssueDto;
use App\UseCases\Gate\Dtos\GateVerifyDto;

/**
 * JWT の発行・検証のユースケースをまとめるサービスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Gate
 */
class GateService extends AbstractService
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
     */
    public function issueToken(GateIssueDto $dto): GateIssueVo
    {
        $raw = $this->issuer->issueForMember($dto->memberId);
        $vo = new GateIssueVo;
        $vo->assign($raw);

        return $vo;
    }

    /**
     * JWT を検証しクレームを返します。
     */
    public function verify(GateVerifyDto $dto): GateVerifyVo
    {
        $raw = $this->verifier->verify($dto->identifier, $dto->token);
        $vo = new GateVerifyVo;
        $vo->assign($raw);

        return $vo;
    }
}
