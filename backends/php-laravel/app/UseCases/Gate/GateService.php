<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Gate;

use App\Domain\Client\Condition\ClientCondition;
use App\Domain\Client\Repositories\ClientRepository;
use App\Domain\Gate\Repositories\GateRepository;
use App\Domain\Gate\ValueObjects\GateIssueVo;
use App\Domain\Gate\ValueObjects\GateVerifyVo;
use App\Domain\Client\Entities\JwtHistory;
use App\Domain\Client\Repositories\JwtHistoryRepository;
use App\Support\Exceptions\AppException;
use App\Support\Mappers\SimpleMapper;
use App\Support\Services\AbstractService;
use App\UseCases\Gate\Dtos\GateIssueDto;
use App\UseCases\Gate\Dtos\GateVerifyDto;
use Carbon\Carbon;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use Illuminate\Support\Str;
use Throwable;

/**
 * 認可Serviceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Gate
 */
class GateService extends AbstractService
{
    /**
     * コンストラクタ。
     *
     * @param ClientRepository $clientRepository クライアントリポジトリ
     * @param GateRepository $gateRepository 認可リポジトリ
     * @param JwtHistoryRepository $historyRepository JWT履歴リポジトリ
     */
    public function __construct(
        private readonly ClientRepository $clientRepository,
        private readonly GateRepository $gateRepository,
        private readonly JwtHistoryRepository $historyRepository,
    ) {
    }

    /**
     * JWTを発行します。
     *
     * @param GateIssueDto $dto JWT発行DTO
     * @return GateIssueVo JWT 発行結果 ValueObject
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException マッピング例外
     */
    public function issueToken(GateIssueDto $dto): GateIssueVo
    {
        $condition = SimpleMapper::map($dto, ClientCondition::class);
        $client = $this->clientRepository->findByAccessToken($condition);
        if (is_null($client)) {
            throw AppException::unauthorized('client_not_found');
        }

        /** @var array{issuer: string, algorithm: string, ttl: int, cache_ttl: int} $configs */
        $configs = config('authorization.app.jwt');
        $identifier = (string)$client->identifier;
        $token = $this->gateRepository->getJwt($identifier, $dto->memberId);

        if ($token === null) {
            $token = $this->issueJwt(
                $configs,
                $dto->memberId,
                $identifier,
                (string)$client->privateKey,
                (string)$client->fingerprint,
            );
            $this->gateRepository->putJwt($identifier, $dto->memberId, $token, $configs['cache_ttl']);

            $history = new JwtHistory();
            $history->clientId = $client->id;
            $history->memberId = $dto->memberId;
            $history->issueAt = Carbon::now();
            $history->jwt = $token;
            $history->assignCreated(0);
            $this->historyRepository->persist($history);
        }

        return new GateIssueVo()->assign(['token' => $token]);
    }

    /**
     * JWTを検証します。
     *
     * @param GateVerifyDto $dto JWT 検証リクエスト用 DTO
     * @return GateVerifyVo JWT 検証結果（Payload 相当）ValueObject
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException マッピング例外
     */
    public function verify(GateVerifyDto $dto): GateVerifyVo
    {
        $condition = SimpleMapper::map($dto, ClientCondition::class);
        $client = $this->clientRepository->findByIdentifier($condition);
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
     * @param array{issuer: string, algorithm: string, ttl: int} $configs JWT 設定
     * @param string $memberId クライアント会員ID（sub）
     * @param string $identifier クライアント識別名（aud）
     * @param string $privateKey 署名用 RSA 秘密鍵（PEM 形式）
     * @param string $fingerprint 秘密鍵フィンガープリント（kid）
     * @return string 発行した JWT 文字列
     */
    private function issueJwt(
        array $configs,
        string $memberId,
        string $identifier,
        string $privateKey,
        string $fingerprint
    ): string {
        $now = time();
        $payload = [
            'iss' => $configs['issuer'],
            'sub' => $memberId,
            'aud' => $identifier,
            'exp' => $now + $configs['ttl'],
            'iat' => $now,
            'nbf' => $now,
            'jti' => (string)Str::uuid(),
        ];

        return JWT::encode($payload, $privateKey, $configs['algorithm'], $fingerprint);
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
