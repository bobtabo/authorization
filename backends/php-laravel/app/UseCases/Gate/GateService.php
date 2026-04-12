<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Gate;

use App\Domain\Client\Repositories\ClientRepository;
use App\Domain\Gate\ValueObjects\GateIssueVo;
use App\Domain\Gate\ValueObjects\GateVerifyVo;
use App\Infrastructure\Gate\GateCacheRepository;
use App\Support\Exceptions\AppException;
use App\Support\Services\AbstractService;
use App\UseCases\Gate\Dtos\GateIssueDto;
use App\UseCases\Gate\Dtos\GateVerifyDto;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use Illuminate\Support\Str;
use Throwable;

/**
 * JWT の発行・検証のユースケースをまとめるサービスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Gate
 */
class GateService extends AbstractService
{
    /**
     * @param ClientRepository $clientRepository クライアントリポジトリ
     * @param GateCacheRepository $cache JWT キャッシュリポジトリ
     */
    public function __construct(
        private readonly ClientRepository $clientRepository,
        private readonly GateCacheRepository $cache,
    ) {
    }

    /**
     * 会員ID に紐づく JWT 発行結果を取得します。
     *
     * @throws AppException クライアントが存在しない場合
     */
    public function issueToken(GateIssueDto $dto): GateIssueVo
    {
        $client = $this->clientRepository->findByAccessToken($dto->accessToken);
        if (is_null($client)) {
            throw AppException::unauthorized('client_not_found');
        }

        /** @var array{issuer: string, algorithm: string, ttl: int, cache_ttl: int} $jwt */
        $jwt = config('authorization.app.jwt');
        $identifier = (string)$client->identifier;
        $token = $this->cache->getJwt($identifier, $dto->memberId);

        if ($token === null) {
            $token = $this->issueJwt(
                $jwt,
                $dto->memberId,
                $identifier,
                (string)$client->privateKey,
                (string)$client->fingerprint,
            );
            $this->cache->putJwt($identifier, $dto->memberId, $token, $jwt['cache_ttl']);
        }

        $vo = new GateIssueVo();
        $vo->assign(['token' => $token]);

        return $vo;
    }

    /**
     * JWT を検証しクレームを返します。
     *
     * @throws AppException クライアントが存在しない場合
     */
    public function verify(GateVerifyDto $dto): GateVerifyVo
    {
        $client = $this->clientRepository->findByIdentifier($dto->identifier);
        if (is_null($client)) {
            throw AppException::forbidden('client_not_found');
        }

        $payload = $this->verifyJwt(
            $dto->identifier,
            $dto->token,
            (string)$client->publicKey,
        );

        $vo = new GateVerifyVo();
        $vo->assign($payload);

        return $vo;
    }

    /**
     * RS256 で署名した JWT を発行します。
     *
     * @param array{issuer: string, algorithm: string, ttl: int} $jwt JWT 設定
     * @param string $memberId クライアント会員ID（sub）
     * @param string $identifier クライアント識別名（aud）
     * @param string $privateKey 署名用 RSA 秘密鍵（PEM 形式）
     * @param string $fingerprint 秘密鍵フィンガープリント（kid）
     * @return string 発行した JWT 文字列
     */
    private function issueJwt(
        array $jwt,
        string $memberId,
        string $identifier,
        string $privateKey,
        string $fingerprint
    ): string {
        $now = time();
        $payload = [
            'iss' => $jwt['issuer'],
            'sub' => $memberId,
            'aud' => $identifier,
            'exp' => $now + $jwt['ttl'],
            'iat' => $now,
            'nbf' => $now,
            'jti' => (string)Str::uuid(),
        ];

        return JWT::encode($payload, $privateKey, $jwt['algorithm'], $fingerprint);
    }

    /**
     * JWT を検証しデコードした Payload を返します。
     *
     * @param string $identifier クライアント識別名（aud 検証に使用）
     * @param string $token JWT 文字列
     * @param string $publicKey 検証用 RSA 公開鍵（PEM 形式）
     * @return array<string, mixed> デコードされた Payload
     * @throws AppException JWT が無効な場合
     */
    private function verifyJwt(string $identifier, string $token, string $publicKey): array
    {
        /** @var array{issuer: string, algorithm: string} $jwt */
        $jwt = config('authorization.app.jwt');

        try {
            $decoded = JWT::decode($token, new Key($publicKey, $jwt['algorithm']));
        } catch (Throwable) {
            throw AppException::unauthorized('jwt_invalid');
        }

        $payload = (array)$decoded;

        if (($payload['iss'] ?? '') !== $jwt['issuer']) {
            throw AppException::unauthorized('jwt_invalid');
        }

        if (($payload['aud'] ?? '') !== $identifier) {
            throw AppException::forbidden('jwt_invalid');
        }

        return $payload;
    }
}
