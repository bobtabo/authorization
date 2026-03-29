<?php
/**
 * This is a program developed by Strategic Insights, Inc.
 *
 * Copyright (c) Strategic Insights, Inc. All Rights Reserved.
 */
namespace SApp\Support\Models;

/**
 * 基底トランザクションModelクラスです。
 *
 * @author Satoshi Nagashiba <nagashibas@sii-japan.co.jp>
 * @package App\Support\Models
 */
class AppTransactionModel extends AppModel
{
    /**
     * {@inheritdoc}
     */
    public bool $cached = true;
}
