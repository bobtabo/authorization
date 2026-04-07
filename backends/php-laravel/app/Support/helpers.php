<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

use Illuminate\Mail\Mailable;
use Illuminate\Support\Facades\Mail;

if (!function_exists('send_mail')) {
    /**
     * メールを送信します。
     *
     * @param string $to 宛先
     * @param Mailable $mailable メールオブジェクト
     * @return void
     */
    function send_mail(string $to, Mailable $mailable): void
    {
        Mail::to($to)->send($mailable);
    }
}

if (!function_exists('get_mail_subject')) {
    /**
     * メールタイトルを取得します。
     *
     * @param string $subject メールタイトル
     * @return string メールタイトル
     */
    function get_mail_subject(string $subject): string
    {
        $environment = app()->display();
        return empty($environment) ? $subject : '【' . $environment . '】' . $subject;
    }
}

if (!function_exists('get_app_url')) {
    /**
     * URLを取得します。
     *
     * @param string|null $uri 相対URL
     * @return string URL
     */
    function get_app_url(?string $uri = null): string
    {
        return url($uri);
    }
}
