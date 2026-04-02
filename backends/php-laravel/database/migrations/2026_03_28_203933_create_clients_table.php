<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * クライアントMigrationクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('clients', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->string('name', 255)->comment('クライアント名');
            $table->string('identifer', 255)->comment('クライアント識別名');
            $table->string('post_code', 8)->comment('郵便番号');
            $table->string('pref', 50)->comment('都道府県');
            $table->string('city', 100)->comment('市区町村');
            $table->string('address', 255)->comment('丁目・番地');
            $table->string('building', 255)->comment('ビル名');
            $table->string('tel', 255)->comment('電話番号');
            $table->string('email', 255)->comment('メールアドレス');
            $table->string('access_token', 512)->comment('アクセストークン');
            $table->string('private_key')->default(1)->comment('秘密鍵');
            $table->string('public_key')->comment('公開鍵');
            $table->string('fingerprint')->comment('フィンガープリント');
            $table->integer('status')->unsigned()->comment('状態');
            $table->timestamp('start_at')->nullable()->comment('利用開始日時');
            $table->timestamp('stop_at')->nullable()->comment('利用停止日時');
            $table->timestamp('created_at')->useCurrent()->comment('登録日時');
            $table->integer('created_by')->unsigned()->comment('登録者ID');
            $table->timestamp('updated_at')->useCurrent()->comment('更新日時');
            $table->integer('updated_by')->unsigned()->comment('更新者ID');
            $table->timestamp('deleted_at')->nullable()->comment('削除日時');
            $table->integer('deleted_by')->unsigned()->nullable()->comment('削除者ID');
            $table->integer('version')->unsigned()->default(1)->comment('バージョン');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('clients');
    }
};
