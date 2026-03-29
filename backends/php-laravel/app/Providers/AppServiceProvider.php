<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Providers;

use App\Domain\Account\Repositories\AccountRepositoryInterface;
use App\Domain\Auth\Repositories\AuthUserRepositoryInterface;
use App\Domain\Client\Repositories\ClientRepositoryInterface;
use App\Domain\Gate\JwtIssuerInterface;
use App\Domain\Gate\JwtVerifierInterface;
use App\Domain\Invitation\Repositories\InvitationRepositoryInterface;
use App\Domain\Notification\Repositories\NotificationRepositoryInterface;
use App\Infrastructure\Gate\StubJwtIssuer;
use App\Infrastructure\Gate\StubJwtVerifier;
use App\Infrastructure\Persistence\Eloquent\Repositories\EloquentAccountRepository;
use App\Infrastructure\Persistence\Eloquent\Repositories\EloquentAuthUserRepository;
use App\Infrastructure\Persistence\Eloquent\Repositories\EloquentClientRepository;
use App\Infrastructure\Persistence\Eloquent\Repositories\StubInvitationRepository;
use App\Infrastructure\Persistence\Eloquent\Repositories\StubNotificationRepository;
use App\UseCases\Account\AccountApplicationService;
use App\UseCases\Auth\AuthApplicationService;
use App\UseCases\Client\ClientApplicationService;
use App\UseCases\Gate\GateApplicationService;
use App\UseCases\Invitation\InvitationApplicationService;
use App\UseCases\Notification\NotificationApplicationService;
use Illuminate\Support\ServiceProvider;

/**
 * ドメインのポート実装とアプリケーションサービスを登録するServiceProviderクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Providers
 */
class AppServiceProvider extends ServiceProvider
{
    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function register(): void
    {
        // Domain ポート → Infrastructure 実装（DIP）
        $this->app->bind(AuthUserRepositoryInterface::class, EloquentAuthUserRepository::class);
        $this->app->bind(ClientRepositoryInterface::class, EloquentClientRepository::class);
        $this->app->bind(AccountRepositoryInterface::class, EloquentAccountRepository::class);
        $this->app->bind(InvitationRepositoryInterface::class, StubInvitationRepository::class);
        $this->app->bind(NotificationRepositoryInterface::class, StubNotificationRepository::class);
        $this->app->bind(JwtIssuerInterface::class, StubJwtIssuer::class);
        $this->app->bind(JwtVerifierInterface::class, StubJwtVerifier::class);

        // アプリケーションサービス（ユースケース）
        $this->app->singleton(AuthApplicationService::class);
        $this->app->singleton(ClientApplicationService::class);
        $this->app->singleton(AccountApplicationService::class);
        $this->app->singleton(InvitationApplicationService::class);
        $this->app->singleton(NotificationApplicationService::class);
        $this->app->singleton(GateApplicationService::class);
    }

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function boot(): void
    {
        //
    }
}
