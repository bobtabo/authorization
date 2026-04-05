<?php
/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
namespace App\Domain\Client\ValueObjects;

use App\Domain\Client\Entities\Client;
use App\Support\Traits\Getter;
use App\Support\ValueObjects\AbstractValueObject;
use Carbon\Carbon;
use Illuminate\Support\Collection;
use Illuminate\Support\Str;
use ReflectionClass;

/**
 * クライアント一覧の結果 ValueObject です。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 * @package App\Domain\Client\ValueObjects
 *
 * @method Collection getClients()
 */
class ClientListVo extends AbstractValueObject
{
    use Getter;

    private Collection $clients;

    /**
     * {@inheritdoc}
     */
    #[\Override]
    public function attributes(): array
    {
        $items = [];
        foreach ($this->clients as $row) {
            $ref = new ReflectionClass($row);
            $item = [];
            foreach ($ref->getProperties() as $prop) {
                $prop->setAccessible(true);
                $item[Str::snake($prop->getName())] = $prop->getValue($row);
            }
            $items[] = $item;
        }
        return ['items' => $items];
    }

    /**
     * クライアント Entity のリストを一覧行に設定します。
     *
     * @param  iterable<int, Client>  $list  Entity の配列または Collection
     */
    public function assignClients(iterable $list): void
    {
        foreach ($list as $entity) {
            $this->clients->add(
                new class($entity) {
                    private ?int $id = null;
                    private ?string $name = null;
                    private ?string $identifier = null;
                    private ?string $postCode = null;
                    private ?string $pref = null;
                    private ?string $city = null;
                    private ?string $address = null;
                    private ?string $building = null;
                    private ?string $tel = null;
                    private ?string $email = null;
                    private ?int $status = null;
                    private ?Carbon $startAt = null;
                    private ?Carbon $stopAt = null;
                    private ?Carbon $createdAt = null;
                    private ?Carbon $updatedAt = null;

                    public function __construct(Client $entity)
                    {
                        $this->id = $entity->id;
                        $this->name = $entity->name;
                        $this->identifier = $entity->identifer;
                        $this->pref = $entity->pref;
                        $this->city = $entity->city;
                        $this->address = $entity->address;
                        $this->building = $entity->building;
                        $this->tel = $entity->tel;
                        $this->email = $entity->email;
                        $this->status = $entity->status?->value;
                        $this->startAt = $entity->startAt;
                        $this->stopAt = $entity->stopAt;
                        $this->createdAt = $entity->createdAt;
                        $this->updatedAt = $entity->updatedAt;
                    }
                }
            );
        }
    }
}
