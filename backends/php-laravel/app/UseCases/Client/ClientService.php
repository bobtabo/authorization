<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Client;

use App\Domain\Client\Condition\ClientCondition;
use App\Domain\Client\Entities\Client;
use App\Domain\Client\Repositories\ClientRepository;
use App\Domain\Client\ValueObjects\ClientDetailVo;
use App\Domain\Client\ValueObjects\ClientListVo;
use App\Domain\Client\ValueObjects\ClientStoreVo;
use App\Support\Mappers\SimpleMapper;
use App\Support\Services\AbstractService;
use App\UseCases\Client\Dtos\ClientDto;

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
    ) {}

    /**
     * クライアント一覧を取得します。
     *
     * @param ClientDto $dto クライアントDTO
     * @return ClientListVo
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

        $result = new ClientListVo;
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
        $condition = SimpleMapper::map($dto, ClientCondition::class);

        $entity = $this->clientRepository->findByCondition($condition);

        $vo = new ClientDetailVo;
        if ($entity === null) {
            return $vo;
        }

        $vo->assign($entity->attributes());

        return $vo->assign($entity->attributes());
    }

    /**
     * クライアントを登録します。
     *
     * @param ClientDto $dto クライアントDTO
     * @return ClientStoreVo クライアント登録ValueObject
     */
    public function store(ClientDto $dto): ClientStoreVo
    {
        $entity = new Client;
        $entity->assign($dto->attributes());

        // TODO 識別子、キーペア、フィンガープリント、トークンの作成
        $token = null;
        $saved = $this->clientRepository->save($entity, $dto->executorId);

        $configs = config('authorization.app.mail');
        return (new ClientStoreVo)->assign([
            'from' => $configs['from'],
            'to' => $saved->email,
            'subject' => get_mail_subject($configs['subject']['prefix'] . $configs['subject']['access_token']),
            'template' => $configs['template']['login'],
            'accessToken' => $token,
        ]);
    }

    /**
     * クライアントを更新します。
     *
     * @param ClientDto $dto クライアントDTO
     * @return ClientStoreVo クライアント登録ValueObject
     */
    public function update(ClientDto $dto): ClientStoreVo
    {
        $condition = SimpleMapper::map($dto, ClientCondition::class);

        $entity = $this->clientRepository->findById($condition);
        $entity->assign($dto->attributes());

        $saved = $this->clientRepository->save($entity, $dto->executorId);

        return (new ClientStoreVo)->assign($saved->attributes());
    }

    /**
     * クライアントを削除します。
     *
     * @param ClientDto $dto クライアントDTO
     * @return void
     */
    public function destroy(ClientDto $dto): void
    {
        $this->clientRepository->delete($dto->id, $dto->executorId);
    }
}
