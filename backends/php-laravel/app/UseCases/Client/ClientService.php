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
use App\Domain\Client\ValueObjects\ClientMutationVo;
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
     * @param ClientDto $dto
     * @return ClientListVo
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException
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
     * @param ClientDto $dto
     * @return ClientDetailVo
     * @throws \AutoMapperPlus\Exception\UnregisteredMappingException
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
     *
     * @param ClientDto $dto
     * @return ClientMutationVo
     */
    public function store(ClientDto $dto): ClientMutationVo
    {
        $entity = new Client;
        $entity->assign($dto->attributes());

        $saved = $this->clientRepository->save($entity, $dto->executorId);

        return (new ClientMutationVo)->assign($saved->attributes());
    }

    /**
     * @param ClientDto $dto
     * @return ClientMutationVo
     */
    public function update(ClientDto $dto): ClientMutationVo
    {
        $condition = SimpleMapper::map($dto, ClientCondition::class);

        $entity = $this->clientRepository->findById($condition);
        $entity->assign($dto->attributes());

        $saved = $this->clientRepository->save($entity, $dto->executorId);

        return (new ClientMutationVo)->assign($saved->attributes());
    }

    /**
     *
     * @param ClientDto $dto
     * @return void
     */
    public function destroy(ClientDto $dto): void
    {
        $this->clientRepository->delete($dto->id, $dto->executorId);
    }
}
