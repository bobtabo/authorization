<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * JWT発行履歴Migrationクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
return new class extends Migration {
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('jwt_issue_histories', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->bigInteger('client_id')->unsigned()->comment('クライアントID');
            $table->string('member_id', 255)->comment('会員ID');
            $table->timestamp('issue_at')->comment('発行日時');
            $table->text('jwt')->comment('JWT');
            $table->timestamp('created_at')->useCurrent()->comment('登録日時');
            $table->integer('created_by')->unsigned()->comment('登録者ID');
            $table->timestamp('updated_at')->useCurrent()->comment('更新日時');
            $table->integer('updated_by')->unsigned()->comment('更新者ID');
            $table->timestamp('deleted_at')->nullable()->comment('削除日時');
            $table->integer('deleted_by')->unsigned()->nullable()->comment('削除者ID');
            $table->integer('version')->unsigned()->default(1)->comment('バージョン');
            $table->foreign('client_id')->references('id')->on('clients');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('jwt_issue_histories');
    }
};
