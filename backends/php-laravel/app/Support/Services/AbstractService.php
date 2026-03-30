<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Services;

use Illuminate\Foundation\Application;

/**
 * 基底Serviceクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Services
 */
abstract class AbstractService
{
    /**
     * アプリケーション
     *
     * @var \Illuminate\Foundation\Application
     */
    protected $app = null;

    /**
     * コンストラクタ
     *
     * @param Application $app アプリケーション
     */
    public function __construct(Application $app)
    {
        $this->app = $app;
    }
}
