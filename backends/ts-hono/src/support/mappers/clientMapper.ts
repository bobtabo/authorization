/**
 * クライアントマッパー定義モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { createMap, forMember, mapFrom } from "@automapper/core";
import { PojosMetadataMap } from "@automapper/pojos";
import { mapper } from "../mapper.js";
import type { Client } from "../../domain/client/entity.js";
import type { ClientListItem, ClientDetailVo, ClientStoreResultVo } from "../../domain/client/valueObjects.js";

export const ClientSymbol = "Client";
export const ClientListItemSymbol = "ClientListItem";
export const ClientDetailVoSymbol = "ClientDetailVo";
export const ClientStoreResultVoSymbol = "ClientStoreResultVo";

PojosMetadataMap.create<Client>(ClientSymbol, {
  id: Number,
  name: String,
  identifier: String,
  postCode: String,
  pref: String,
  city: String,
  address: String,
  building: String,
  tel: String,
  email: String,
  status: Number,
  token: String,
  publicKey: String,
  privateKey: String,
  fingerprint: String,
  startedAt: () => Date,
  stoppedAt: () => Date,
  createdAt: () => Date,
  createdBy: Number,
  updatedAt: () => Date,
  updatedBy: Number,
  deletedAt: () => Date,
  deletedBy: Number,
  version: Number,
});

PojosMetadataMap.create<ClientListItem>(ClientListItemSymbol, {
  id: Number,
  name: String,
  identifier: String,
  status: Number,
  startedAt: () => Date,
  stoppedAt: () => Date,
  createdAt: () => Date,
  updatedAt: () => Date,
});

PojosMetadataMap.create<ClientDetailVo>(ClientDetailVoSymbol, {
  id: Number,
  name: String,
  identifier: String,
  postCode: String,
  pref: String,
  city: String,
  address: String,
  building: String,
  tel: String,
  email: String,
  status: Number,
  fingerprint: String,
  startedAt: () => Date,
  stoppedAt: () => Date,
  createdAt: () => Date,
  updatedAt: () => Date,
});

PojosMetadataMap.create<ClientStoreResultVo>(ClientStoreResultVoSymbol, {
  id: Number,
  name: String,
  identifier: String,
  email: String,
  token: String,
});

createMap<Client, ClientListItem>(
  mapper, ClientSymbol, ClientListItemSymbol,
  forMember(d => d.status, mapFrom(s => s.status ?? 1)),
);

createMap<Client, ClientDetailVo>(
  mapper, ClientSymbol, ClientDetailVoSymbol,
  forMember(d => d.postCode,  mapFrom(s => s.postCode  ?? "")),
  forMember(d => d.pref,      mapFrom(s => s.pref      ?? "")),
  forMember(d => d.city,      mapFrom(s => s.city      ?? "")),
  forMember(d => d.address,   mapFrom(s => s.address   ?? "")),
  forMember(d => d.building,  mapFrom(s => s.building  ?? "")),
  forMember(d => d.tel,       mapFrom(s => s.tel       ?? "")),
  forMember(d => d.email,     mapFrom(s => s.email     ?? "")),
  forMember(d => d.status,    mapFrom(s => s.status    ?? 1)),
);

createMap<Client, ClientStoreResultVo>(
  mapper, ClientSymbol, ClientStoreResultVoSymbol,
  forMember(d => d.email, mapFrom(s => s.email ?? "")),
  forMember(d => d.token, mapFrom(s => s.token ?? "")),
);
