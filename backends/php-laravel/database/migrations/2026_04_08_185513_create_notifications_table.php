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
 * 通知Migrationクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
return new class extends Migration {
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('notifications', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->integer('staff_id')->unsigned()->comment('スタッフID');
            $table->integer('message_type')->unsigned()->comment('メッセージ種類');
            $table->string('title', 255)->comment('タイトル');
            $table->string('message', 512)->comment('メッセージ');
            $table->string('url', 255)->nullable()->comment('URL');
            $table->tinyInteger('read')->unsigned()->default(0)->comment('既読');
            $table->timestamp('created_at')->useCurrent()->comment('登録日時');
            $table->integer('created_by')->unsigned()->comment('登録者ID');
            $table->timestamp('updated_at')->useCurrent()->comment('更新日時');
            $table->integer('updated_by')->unsigned()->comment('更新者ID');
            $table->timestamp('deleted_at')->nullable()->comment('削除日時');
            $table->integer('deleted_by')->unsigned()->nullable()->comment('削除者ID');
            $table->integer('version')->unsigned()->comment('バージョン');
            $table->foreign('staff_id')->references('id')->on('staffs');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('notifications');
    }
};
