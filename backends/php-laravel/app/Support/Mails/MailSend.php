<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\Support\Mails;

use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;

/**
 * メール送信ValueObjectクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Mails
 *
 * @method string|null getHost()
 * @method string|null getFrom()
 * @method string|null getFromName()
 * @method string|null getTo()
 * @method array getCc()
 * @method array getBcc()
 * @method string|null getSubject()
 * @method int|null getPort()
 * @method string|null getTemplate()
 * @method bool isSend()
 */
class MailSend extends AbstractValueObject
{
    use Getter;

    protected ?string $host = null;
    protected ?string $from = null;
    protected ?string $fromName = null;
    protected ?string $to = null;
    protected array $cc = [];
    protected array $bcc = [];
    protected ?string $subject = null;
    protected ?int $port = null;
    protected ?string $template = null;
    protected bool $send = true;

    /**
     * コンストラクタ
     */
    public function __construct()
    {
        parent::__construct();

        $this->fromName = config('mail.from.name');
    }
}
