<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Models;

/**
 * 基底マスタModelクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Models
 */
class AppMasterModel extends AppModel
{
    /**
     * {@inheritdoc}
     */
    public bool $cached = true;
}
