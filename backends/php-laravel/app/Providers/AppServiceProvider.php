<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Providers;

use App\Domain\Client\Repositories\ClientRepository;
use App\Domain\Gate\JwtIssuerInterface;
use App\Domain\Gate\JwtVerifierInterface;
use App\Domain\Invitation\Repositories\InvitationRepositoryInterface;
use App\Domain\Notification\Repositories\NotificationRepository;
use App\Domain\Staff\Repositories\StaffRepository;
use App\Infrastructure\Gate\StubJwtIssuer;
use App\Infrastructure\Gate\StubJwtVerifier;
use App\Infrastructure\Repositories\EloquentClientEloquentRepository;
use App\Infrastructure\Repositories\EloquentInvitationEloquentRepository;
use App\Infrastructure\Repositories\EloquentNotificationRepository;
use App\Infrastructure\Repositories\EloquentStaffEloquentRepository;
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
        $this->app->bind(ClientRepository::class, EloquentClientEloquentRepository::class);
        $this->app->bind(StaffRepository::class, EloquentStaffEloquentRepository::class);
        $this->app->bind(InvitationRepositoryInterface::class, EloquentInvitationEloquentRepository::class);
        $this->app->bind(NotificationRepository::class, EloquentNotificationRepository::class);
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
                $status
            );
        });

        // Error (4xx, 5xx)
        Response::macro('failure', function (
            string $message = 'データが存在しません。',
            int $status = ResponseStatus::HTTP_NOT_FOUND
        ) {
            return response()->json([
                'message' => $message,
            ], $status);
        });

        // バリデーションの詳細付きエラー
        Response::macro('errors', function (
            array $errors,
            int $status = ResponseStatus::HTTP_UNPROCESSABLE_ENTITY
        ) {
            return response()->json([
                'errors' => $errors,
            ], $status);
        });
    }
}
