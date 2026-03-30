<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\UseCases\Client;

use App\Domain\Client\Condition\ClientCondition;
use App\Domain\Client\Entities\Client;
use App\Domain\Client\Mappers\ClientApiMapper;
use App\Domain\Client\Repositories\ClientRepositoryInterface;
use App\Domain\Client\ValueObjects\ClientDetailVo;
use App\Domain\Client\ValueObjects\ClientListVo;
use App\Domain\Client\ValueObjects\ClientMutationVo;
use App\Domain\Client\ValueObjects\ClientRemoveVo;
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
        private readonly ClientRepositoryInterface $clients,
    ) {}

    /**
     * クライアント一覧を取得します。
     */
    public function index(ClientDto $dto): ClientListVo
    {
        /** @var ClientCondition $condition */
        $condition = SimpleMapper::mapSpecific($dto, ClientCondition::class, [
            'keyword' => 'keyword',
            'startFrom' => 'startFrom',
            'startTo' => 'startTo',
        ]);
        $condition->statuses = $dto->statuses ?? [];

        $list = $this->clients->searchByCondition($condition);

        $result = new ClientListVo;
        $result->assignClients($list);

        return $result;
    }

    /**
     * クライアント詳細を取得します。
     */
    public function show(ClientDto $dto): ClientDetailVo
    {
        /** @var ClientCondition $condition */
        $condition = SimpleMapper::map($dto, ClientCondition::class);

        $entity = $this->clients->findByCondition($condition);

        $vo = new ClientDetailVo;
        if ($entity === null) {
            return $vo;
        }

        $vo->found = true;
        $vo->client = ClientApiMapper::toResponseArray($entity);

        return $vo;
    }

    public function store(ClientDto $dto): ClientMutationVo
    {
        $entity = new Client;
        $entity->assign($this->storePayloadToEntity($dto));

        $saved = $this->clients->persist($entity, $dto->executorId);

        $vo = new ClientMutationVo;
        $vo->message = 'SUCCESS';
        $vo->client = ClientApiMapper::toResponseArray($saved);

        return $vo;
    }

    public function update(ClientDto $dto): ClientMutationVo
    {
        $vo = new ClientMutationVo;
        $vo->ok = false;
        if ($dto->id === null) {
            return $vo;
        }

        $existing = $this->clients->findById($dto->id);
        if ($existing === null) {
            return $vo;
        }

        $patch = $this->updatePayloadToEntity($dto);
        if ($patch !== []) {
            $existing->assign($patch);
        }

        $saved = $this->clients->persist($existing, $dto->executorId);
        $vo->ok = true;
        $vo->message = 'SUCCESS';
        $vo->client = ClientApiMapper::toResponseArray($saved);

        return $vo;
    }

    public function destroy(ClientDto $dto): ClientRemoveVo
    {
        $vo = new ClientRemoveVo;
        if ($dto->id === null) {
            return $vo;
        }

        $ok = $this->clients->softDelete($dto->id, $dto->executorId);
        $vo->ok = $ok;
        $vo->message = 'SUCCESS';

        return $vo;
    }

    /**
     * @return array<string, mixed>
     */
    private function storePayloadToEntity(ClientDto $dto): array
    {
        $map = [
            'name' => 'name',
            'identifier' => 'identifer',
            'post_code' => 'postCode',
            'pref' => 'pref',
            'city' => 'city',
            'address' => 'address',
            'building' => 'building',
            'tel' => 'tel',
            'email' => 'email',
        ];
        $attrs = $dto->attributes();
        unset($attrs['executorId'], $attrs['id'], $attrs['keyword'], $attrs['startFrom'], $attrs['startTo'], $attrs['statuses'], $attrs['status'], $attrs['version']);
        $out = [];
        foreach ($map as $requestKey => $prop) {
            if (! array_key_exists($requestKey, $attrs)) {
                continue;
            }
            $out[$prop] = $attrs[$requestKey];
        }

        return $out;
    }

    /**
     * @return array<string, mixed>
     */
    private function updatePayloadToEntity(ClientDto $dto): array
    {
        $map = [
            'name' => 'name',
            'identifier' => 'identifer',
            'post_code' => 'postCode',
            'pref' => 'pref',
            'city' => 'city',
            'address' => 'address',
            'building' => 'building',
            'tel' => 'tel',
            'email' => 'email',
            'status' => 'status',
        ];
        $attrs = $dto->attributes();
        unset($attrs['executorId'], $attrs['id'], $attrs['keyword'], $attrs['startFrom'], $attrs['startTo'], $attrs['statuses'], $attrs['version']);
        $out = [];
        foreach ($map as $requestKey => $prop) {
            if (! array_key_exists($requestKey, $attrs)) {
                continue;
            }
            $value = $attrs[$requestKey];
            if ($requestKey === 'status' && $value === null) {
                continue;
            }
            $out[$prop] = $value;
        }

        return $out;
    }
}
