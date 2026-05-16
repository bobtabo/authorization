<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Invitation;

use App\Domain\Invitation\Condition\InvitationCondition;
use App\Domain\Invitation\Repositories\InvitationAuthRepository;
use App\Domain\Invitation\Repositories\InvitationRepository;
use App\Domain\Invitation\ValueObjects\InvitationVo;
use App\Support\Exceptions\AppException;
use App\Support\Mappers\SimpleMapper;
use App\Support\Services\AbstractService;
use App\UseCases\Invitation\Dtos\InvitationDto;
use Random\RandomException;

/**
 * 招待Serviceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Invitation
 */
class InvitationService extends AbstractService
{
    /**
     * コンストラクタ。
     *
     * @param InvitationRepository $invitationRepository 招待Repository
     * @param InvitationAuthRepository $invitationAuthRepository 招待認証Repository
     */
    public function __construct(
        private readonly InvitationRepository $invitationRepository,
        private readonly InvitationAuthRepository $invitationAuthRepository,
    ) {
    }

    /**
     * 現在の招待情報を取得します。
     *
     * @param InvitationDto $dto 招待DTO
     * @return InvitationVo 招待ValueObject
     */
    public function current(InvitationDto $dto): InvitationVo
    {
        $entity = $this->invitationRepository->getCurrentByRole($dto->role ?? 2);
        if ($entity === null) {
            throw AppException::notFound('invitation_not_found');
        }

        $url = $this->buildUrl($entity->token);
        return new InvitationVo()->assign([
            'found' => true,
            'url' => $url,
            'displayUrl' => $this->buildDisplayUrl($url),
            'token' => $entity->token,
        ]);
    }

    /**
     * 新しい招待を発行します。
     *
     * @param InvitationDto $dto 招待DTO
     * @return InvitationVo 招待ValueObject
     * @throws RandomException 乱数生成に失敗した場合（永続化実装に依存）
     */
    public function issue(InvitationDto $dto): InvitationVo
    {
        $entity = $this->invitationRepository->getCurrentByRole($dto->role ?? 2);
        if ($entity === null) {
            throw AppException::notFound('invitation_not_found');
        }
        $entity->token = bin2hex(random_bytes(16));
        $entity->assignUpdated($dto->executorId);
        $saved = $this->invitationRepository->persist($entity);

        $url = $this->buildUrl($saved->token);
        return new InvitationVo()->assign([
            'found' => true,
            'url' => $url,
            'displayUrl' => $this->buildDisplayUrl($url),
            'token' => $saved->token,
        ]);
    }

    /**
     * 招待トークンから招待情報を解決します。
     *
     * @param InvitationDto $dto 招待DTO
     * @return InvitationVo 招待ValueObject
     */
    public function findByToken(InvitationDto $dto): InvitationVo
    {
        $token = $dto->token;
        if (!is_string($token) || $token === '') {
            throw AppException::badRequest('invitation_invalid');
        }

        $condition = SimpleMapper::map($dto, InvitationCondition::class);
        $entity = $this->invitationRepository->findByToken($condition);
        if ($entity === null) {
            throw AppException::badRequest('invitation_invalid');
        }

        // 招待トークンとロールを一時保存（10分間）
        $this->invitationAuthRepository->store($entity->token, $entity->role ?? 2, 600);

        $url = $this->buildUrl($entity->token);
        return new InvitationVo()->assign([
            'found' => true,
            'url' => $url,
            'displayUrl' => $this->buildDisplayUrl($url),
            'token' => $entity->token,
        ]);
    }

    /**
     * トークンから完全な招待 URL を生成します。
     *
     * @param string $token 招待トークン
     * @return string 完全 URL
     */
    private function buildUrl(string $token): string
    {
        $base = rtrim((string)config('authorization.app.frontend_url'), '/');
        return $base . '/invitation/' . $token;
    }

    /**
     * 表示用に `/invitation/` 以降のトークンを省略した URL を返します。
     *
     * @param string $url 完全 URL
     * @param int $head トークン先頭から表示する文字数
     * @param int $tail トークン末尾から表示する文字数
     * @return string 省略表示用 URL
     */
    private function buildDisplayUrl(string $url, int $head = 6, int $tail = 4): string
    {
        $segment = '/invitation/';
        $idx = strpos($url, $segment);
        if ($idx === false) {
            return strlen($url) > 72 ? substr($url, 0, 68) . '...' : $url;
        }

        $base = substr($url, 0, $idx + strlen($segment));
        $after = substr($url, $idx + strlen($segment));
        $suffixLen = strcspn($after, '?#');
        $token = substr($after, 0, $suffixLen);
        $suffix = substr($after, $suffixLen);

        if (strlen($token) <= $head + $tail + 3) {
            return $url;
        }

        return $base . substr($token, 0, $head) . '...' . substr($token, -$tail) . $suffix;
    }
}
