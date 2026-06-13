<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

use Aws\Ses\SesClient;
use Illuminate\Mail\Mailable;

if (!function_exists('send_mail')) {
    /**
     * SES でメールを送信します。
     *
     * @param string $to 宛先
     * @param Mailable $mailable メールオブジェクト
     * @return void
     */
    function send_mail(string $to, Mailable $mailable): void
    {
        $mailable->to($to);
        $mailable->build();

        $subject = $mailable->subject;
        $html = view($mailable->view, $mailable->viewData)->render();
        $from = $mailable->from[0]['address'] ?? config('mail.from.address');
        $fromName = $mailable->from[0]['name'] ?? config('mail.from.name');

        $opts = [
            'region'  => config('services.ses.region', env('AWS_REGION', 'ap-northeast-1')),
            'version' => 'latest',
        ];

        $accessKey = env('AWS_ACCESS_KEY_ID', '');
        $secretKey = env('AWS_SECRET_ACCESS_KEY', '');
        if (!empty($accessKey)) {
            $opts['credentials'] = [
                'key'    => $accessKey,
                'secret' => $secretKey,
            ];
        }

        $endpoint = env('AWS_ENDPOINT_URL', '');
        if (!empty($endpoint)) {
            $opts['endpoint'] = $endpoint;
        }

        try {
            $client = new SesClient($opts);
            $client->sendEmail([
                'Source' => "{$fromName} <{$from}>",
                'Destination' => [
                    'ToAddresses' => [$to],
                ],
                'Message' => [
                    'Subject' => [
                        'Data'    => $subject,
                        'Charset' => 'UTF-8',
                    ],
                    'Body' => [
                        'Html' => [
                            'Data'    => $html,
                            'Charset' => 'UTF-8',
                        ],
                    ],
                ],
            ]);
        } catch (\Exception $e) {
            \Log::error('mail send error: ' . $e->getMessage());
        }
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
        return empty($environment) ? $subject : '[' . $environment . ']' . $subject;
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
