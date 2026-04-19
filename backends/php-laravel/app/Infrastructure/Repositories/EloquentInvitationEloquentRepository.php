<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Infrastructure\Repositories;

use App\Domain\Invitation\Condition\InvitationCondition;
use App\Domain\Invitation\Entities\Invitation as Entity;
use App\Domain\Invitation\Repositories\InvitationRepository;
use App\Infrastructure\Models\Invitation as Model;
use App\Support\Repositories\AbstractEloquentRepository;
use Random\RandomException;

/**
 * 永続化未接続時に招待を仮返却するStubのRepositoryクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Infrastructure\Repositories
 */
class EloquentInvitationEloquentRepository extends AbstractEloquentRepository implements InvitationRepository
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function getCurrent(): ?Entity
    {
        $model = Model::query()
            ->whereNull('deleted_at')
            ->orderByDesc('id')
            ->first();

        if ($model === null) {
            return null;
        }

        $url = $this->buildUrl($model->token);
        $entity = new Entity();
        $entity->assign([
            'token' => $model->token,
            'url' => $url,
            'displayUrl' => $this->buildDisplayUrl($url),
        ]);

        return $entity;
    }

    /**
     * {@inheritdoc}
     *
     * @throws RandomException 暗号論的乱数の生成に失敗した場合
     */
    #[\Override]
    public function issue(): Entity
    {
        $token = bin2hex(random_bytes(16));
        $url = $this->buildUrl($token);
        $invitation = new Entity();
        $invitation->assign([
            'token' => $token,
            'url' => $url,
            'displayUrl' => $this->buildDisplayUrl($url),
        ]);

        return $invitation;
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function findByToken(InvitationCondition $condition): ?Entity
    {
        $token = trim($condition->token);
        if ($token === '') {
            return null;
        }

        $url = $this->buildUrl($token);
        $invitation = new Entity();
        $invitation->assign([
            'token' => $token,
            'url' => $url,
            'displayUrl' => $this->buildDisplayUrl($url),
        ]);

        return $invitation;
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

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getModel(): Model
    {
        return new Model();
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    protected function getEntity(): Entity
    {
        return new Entity();
    }
}
