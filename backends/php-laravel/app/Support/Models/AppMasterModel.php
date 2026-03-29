<?php
/**
 * This is a program developed by Strategic Insights, Inc.
 *
 * Copyright (c) Strategic Insights, Inc. All Rights Reserved.
 */
namespace App\Support\Models;

/**
 * 基底マスタModelクラスです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
 * @package App\Support\Models
 */
class AppMasterModel extends AppModel
{
    /**
     * {@inheritdoc}
     */
    public bool $cached = true;
}
