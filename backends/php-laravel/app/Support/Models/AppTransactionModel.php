<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Models;

/**
 * 基底トランザクションModelクラスです。
 *
 * @author @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Models
 */
class AppTransactionModel extends AppModel
{
    /**
     * {@inheritdoc}
     */
    public bool $cached = true;
}
