<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Providers;

use App\Domain\Client\Repositories\ClientRepository;
use App\Domain\Gate\JwtIssuerInterface;
use App\Domain\Gate\JwtVerifierInterface;
use App\Domain\Invitation\Repositories\InvitationRepositoryInterface;
use App\Domain\Notification\Repositories\NotificationRepository;
use App\Domain\Staff\Repositories\StaffRepositoryInterface;
use App\Infrastructure\Gate\StubJwtIssuer;
use App\Infrastructure\Gate\StubJwtVerifier;
use App\Infrastructure\Repositories\CacheNotificationRepository;
use App\Infrastructure\Repositories\EloquentClientRepository;
use App\Infrastructure\Repositories\EloquentInvitationRepository;
use App\Infrastructure\Repositories\EloquentStaffRepository;
use App\UseCases\Auth\AuthService;
use App\UseCases\Client\ClientService;
use App\UseCases\Gate\GateService;
use App\UseCases\Invitation\InvitationService;
use App\UseCases\Notification\NotificationService;
use App\UseCases\Staff\StaffService;
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
        $this->app->bind(ClientRepository::class, EloquentClientRepository::class);
        $this->app->bind(StaffRepositoryInterface::class, EloquentStaffRepository::class);
        $this->app->bind(InvitationRepositoryInterface::class, EloquentInvitationRepository::class);
        $this->app->bind(NotificationRepository::class, CacheNotificationRepository::class);
        $this->app->bind(JwtIssuerInterface::class, StubJwtIssuer::class);
        $this->app->bind(JwtVerifierInterface::class, StubJwtVerifier::class);

        // アプリケーションサービス（ユースケース）
        $this->app->singleton(AuthService::class);
        $this->app->singleton(ClientService::class);
        $this->app->singleton(StaffService::class);
        $this->app->singleton(InvitationService::class);
        $this->app->singleton(NotificationService::class);
        $this->app->singleton(GateService::class);
    }

    /**
     * 全アプリケーションサービスの初期起動処理を行います。
     *
     * @return void
     */
    public function boot(): void
    {
        //
    }
}
