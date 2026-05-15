<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Providers;

use App\Domain\Client\Repositories\ClientRepository;
use App\Domain\Gate\Repositories\GateRepository;
use App\Domain\Invitation\Repositories\InvitationAuthRepository;
use App\Domain\Invitation\Repositories\InvitationRepository;
use App\Domain\Notification\Repositories\NotificationRepository;
use App\Domain\Staff\Repositories\StaffRepository;
use App\Infrastructure\Cache\RedisGateRepository;
use App\Infrastructure\Cache\RedisInvitationAuthRepository;
use App\Infrastructure\Persistence\EloquentClientRepository;
use App\Infrastructure\Persistence\EloquentInvitationRepository;
use App\Infrastructure\Persistence\EloquentNotificationRepository;
use App\Infrastructure\Persistence\EloquentStaffRepository;
use App\UseCases\Auth\AuthService;
use App\UseCases\Client\ClientService;
use App\UseCases\Gate\GateService;
use App\UseCases\Invitation\InvitationService;
use App\UseCases\Notification\NotificationService;
use App\UseCases\Staff\StaffService;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\Response;
use Illuminate\Support\ServiceProvider;
use Symfony\Component\HttpFoundation\Response as ResponseStatus;

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
        $this->app->bind(StaffRepository::class, EloquentStaffRepository::class);
        $this->app->bind(InvitationRepository::class, EloquentInvitationRepository::class);
        $this->app->bind(NotificationRepository::class, EloquentNotificationRepository::class);
        $this->app->bind(GateRepository::class, RedisGateRepository::class);
        $this->app->bind(InvitationAuthRepository::class, RedisInvitationAuthRepository::class);

        // アプリケーションサービス（ユースケース）
        $this->app->singleton(AuthService::class);
        $this->app->singleton(ClientService::class);
        $this->app->singleton(GateService::class);
        $this->app->singleton(InvitationService::class);
        $this->app->singleton(NotificationService::class);
        $this->app->singleton(StaffService::class);
    }

    /**
     * 全アプリケーションサービスの初期起動処理を行います。
     *
     * @return void
     * @method \Illuminate\Http\JsonResponse success(array $data, int $status = 200)
     */
    public function boot(): void
    {
        // Success (200 OK.)
        Response::macro('success', function (array $data = [], int $status = ResponseStatus::HTTP_OK): JsonResponse {
            return response()->json(
                array_merge(
                    [
                        'message' => 'SUCCESS'
                    ],
                    (array)$data
                ),
                $status,
                [],
                JSON_UNESCAPED_UNICODE
            );
        });

        // Error (4xx, 5xx)
        Response::macro('failure', function (
            string $message = 'データが存在しません。',
            int $status = ResponseStatus::HTTP_NOT_FOUND
        ) {
            return response()->json([
                'message' => $message,
            ], $status, ['Content-Type' => 'application/json; charset=utf-8'], JSON_UNESCAPED_UNICODE);
        });

        // バリデーションの詳細付きエラー
        Response::macro('errors', function (
            array $errors,
            int $status = ResponseStatus::HTTP_UNPROCESSABLE_ENTITY
        ) {
            return response()->json([
                'errors' => $errors,
            ], $status, ['Content-Type' => 'application/json; charset=utf-8'], JSON_UNESCAPED_UNICODE);
        });
    }
}
