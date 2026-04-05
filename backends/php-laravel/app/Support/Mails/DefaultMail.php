<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Support\Mails;

use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Queue\SerializesModels;

/**
 * 標準Mailクラスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Support\Mails
 */
class DefaultMail extends Mailable
{
    use Queueable;
    use SerializesModels;

    protected MailSend $mailSend;

    /**
     * コンストラクタ。
     *
     * @param MailSend $mailSend メール送信ValueObject
     */
    public function __construct(MailSend $mailSend)
    {
        $this->mailSend = $mailSend;
    }

    /**
     * メールを作成します。
     *
     * @return Mailable メールオブジェクト
     */
    public function build(): Mailable
    {
        $this->from($this->mailSend->getFrom(), $this->mailSend->getFromName())
            ->to($this->mailSend->getTo())
            ->subject($this->mailSend->getSubject())
            ->view($this->mailSend->getTemplate(), $this->mailSend->attributes());

        foreach ($this->mailSend->getCc() as $cc) {
            $this->cc($cc);
        }

        foreach ($this->mailSend->getBcc() as $bcc) {
            $this->bcc($bcc);
        }

        return $this;
    }
}
