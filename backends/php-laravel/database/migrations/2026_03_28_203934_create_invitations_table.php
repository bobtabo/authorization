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
 * 招待Migrationクラスです。
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
        Schema::create('invitations', function (Blueprint $table) {
            $table->increments('id');
            $table->text('token')->comment('トークン');
            $table->string('created_at')->useCurrent()->comment('登録日時');
            $table->integer('created_by')->unsigned()->comment('登録者ID');
            $table->string('updated_at')->useCurrent()->comment('更新日時');
            $table->integer('updated_by')->unsigned()->comment('更新者ID');
            $table->string('deleted_at')->nullable()->comment('削除日時');
            $table->integer('deleted_by')->unsigned()->nullable()->comment('削除者ID');
            $table->integer('version')->unsigned()->comment('バージョン');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('invitations');
    }
};
