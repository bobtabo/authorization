<?php

/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */

declare(strict_types=1);

namespace App\UseCases\Client;

use App\Domain\Client\Condition\ClientCondition;
use App\Domain\Client\Entities\Client;
use App\Domain\Client\Enums\ClientStatus;
use App\Domain\Client\Repositories\ClientRepository;
use App\Domain\Client\ValueObjects\ClientDetailVo;
use App\Domain\Client\ValueObjects\ClientListVo;
use App\Domain\Client\ValueObjects\ClientStoreVo;
use App\Support\Mappers\SimpleMapper;
use App\Support\Services\AbstractService;
use App\UseCases\Client\Dtos\ClientDto;
use Carbon\Carbon;

/**
 * クライアントのユースケースをまとめるサービスです。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\UseCases\Client
 */
class ClientService extends AbstractService
{
    public function __construct(
        private readonly ClientRepository $clientRepository,
    ) {
    }

    /**
     * アクセストークンでクライアントを認証します。
     *
     * @param ClientDto $dto クライアントDTO
     * @return bool 認証成功の場合 true
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException マッピング例外
     */
    public function authenticateByToken(ClientDto $dto): bool
    {
        $condition = SimpleMapper::map($dto, ClientCondition::class);

        return $this->clientRepository->findByAccessToken($condition) !== null;
    }

    /**
     * クライアント一覧を取得します。
     *
     * @param ClientDto $dto クライアントDTO
     * @return ClientListVo クライアント一覧ValueObject
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException マッピング例外
     */
    public function getClients(ClientDto $dto): ClientListVo
    {
        /** @var ClientCondition $condition */
        $condition = SimpleMapper::mapSpecific($dto, ClientCondition::class, [
            'keyword' => 'keyword',
            'startFrom' => 'startFrom',
            'startTo' => 'startTo',
            'statuses' => 'statuses',
        ]);

        $list = $this->clientRepository->findByCondition($condition);

        $result = new ClientListVo();
        $result->assignClients($list);

        return $result;
    }

    /**
     * クライアント詳細を取得します。
     *
     * @param ClientDto $dto クライアントDTO
     * @return ClientDetailVo クライアント詳細ValueObject
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException マッピング例外
     */
    public function show(ClientDto $dto): ClientDetailVo
    {
        /** @var ClientCondition $condition */
        $dto->statuses = [];
        $condition = SimpleMapper::map($dto, ClientCondition::class);

        $entity = $this->clientRepository->findById($condition);

        $vo = new ClientDetailVo();
        if ($entity === null) {
            return $vo;
        }

        return $vo->assign($entity->attributes());
    }

    /**
     * クライアントを登録します。
     *
     * @param ClientDto $dto クライアントDTO
     * @return ClientStoreVo クライアント登録ValueObject
     * @throws \Random\RandomException ランダム例外
     */
    public function store(ClientDto $dto): ClientStoreVo
    {
        $entity = new Client();
        $entity->assign($dto->attributes());
        $entity->identifier = uniqid();
        $entity->status = ClientStatus::Inactive;

        // RSAキーペアの生成（4096bit、PEM形式）
        // shell 相当: ssh-keygen -t rsa -b 4096 -m PEM
        $keyPair = openssl_pkey_new([
            'private_key_bits' => 4096,
            'private_key_type' => OPENSSL_KEYTYPE_RSA,
        ]);

        // 秘密鍵（PEM）
        // shell 相当: -f "$PRIVATE_PEM" -N ""
        openssl_pkey_export($keyPair, $privateKey);
        $entity->privateKey = $privateKey;

        // 公開鍵（PEM）
        // shell 相当: openssl rsa -pubout -outform PEM
        $details = openssl_pkey_get_details($keyPair);
        $entity->publicKey = $details['key'];

        // SSH フィンガープリント（SHA256）
        // shell 相当: ssh-keygen -l -f "$PRIVATE_PEM"
        // ssh-keygen -l の SHA256 は SSH ワイヤーフォーマット（RFC 4253）の SHA256 ハッシュ
        $type = 'ssh-rsa';
        $blob = pack('N', strlen($type)) . $type
            . pack('N', strlen($details['rsa']['e'])) . $details['rsa']['e']
            . pack('N', strlen($details['rsa']['n'])) . $details['rsa']['n'];
        $entity->fingerprint = 'SHA256:' . rtrim(base64_encode(hash('sha256', $blob, true)), '=');

        $entity->assignCreated($dto->executorId ?? 0);

        // アクセストークンの生成
        $entity->accessToken = bin2hex(random_bytes(32));

        $saved = $this->clientRepository->persist($entity);

        $configs = config('authorization.app.mail');
        return new ClientStoreVo()->assign([
            'id' => $saved->id,
            'name' => $saved->name,
            'from' => $configs['from'],
            'to' => $saved->email,
            'subject' => get_mail_subject($configs['subject']['prefix'] . $configs['subject']['access_token']),
            'template' => $configs['template']['access_token'],
            'accessToken' => $saved->accessToken,
        ]);
    }

    /**
     * クライアントを更新します。
     *
     * @param ClientDto $dto クライアントDTO
     * @return ClientStoreVo クライアント更新ValueObject
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException マッピング例外
     */
    public function update(ClientDto $dto): ClientStoreVo
    {
        $condition = SimpleMapper::map($dto, ClientCondition::class);

        $entity = $this->clientRepository->findById($condition);
        // identifier は登録時に自動生成するため更新不可
        // status は遷移ロジックで個別に制御するため assign から除外
        // accessToken は不変のため assign から除外
        $entity->assign($dto->attributes(), [], ['identifier', 'status', 'accessToken']);

        // ステータス遷移に応じた利用開始・停止日時を自動設定
        if ($dto->status !== null) {
            $entity->status = $dto->status;
            if ($dto->status === ClientStatus::Active && $entity->startAt === null) {
                $entity->startAt = Carbon::now();
                $entity->stopAt = null;
            }
            if ($dto->status === ClientStatus::Suspended) {
                $entity->stopAt = Carbon::now();
            }
        }

        $entity->assignUpdated($dto->executorId ?? 0);

        $saved = $this->clientRepository->persist($entity);

        return new ClientStoreVo()->assign($saved->attributes());
    }

    /**
     * クライアントを論理削除します。
     * 削除前にステータスを Closed（4）に更新します。
     *
     * @param ClientDto $dto クライアントDTO
     * @return void
     */
    public function destroy(ClientDto $dto): void
    {
        $condition = SimpleMapper::map($dto, ClientCondition::class);
        $entity = $this->clientRepository->findById($condition);

        $entity->status = ClientStatus::Closed;
        $entity->assignUpdated($dto->executorId ?? 0);
        $saved = $this->clientRepository->persist($entity);

        $saved->assignDeleted($dto->executorId ?? 0);
        $this->clientRepository->deleteById($saved);
    }
}
