/**
 * スタッフマッパー定義モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { createMap, forMember, mapFrom } from "@automapper/core";
import { PojosMetadataMap } from "@automapper/pojos";
import { mapper } from "../mapper.js";
import type { Staff } from "../../domain/staff/entity.js";
import type { StaffListItem, StaffVo } from "../../domain/staff/valueObjects.js";

export const StaffSymbol = "Staff";
export const StaffListItemSymbol = "StaffListItem";
export const StaffVoSymbol = "StaffVo";

PojosMetadataMap.create<Staff>(StaffSymbol, {
  id: Number,
  name: String,
  email: String,
  provider: Number,
  providerId: String,
  avatar: String,
  role: Number,
  lastLoginAt: () => Date,
  createdAt: () => Date,
  createdBy: Number,
  updatedAt: () => Date,
  updatedBy: Number,
  deletedAt: () => Date,
  version: Number,
});

PojosMetadataMap.create<StaffListItem>(StaffListItemSymbol, {
  id: Number,
  name: String,
  email: String,
  role: Number,
  status: Number,
  createdAt: () => Date,
  updatedAt: () => Date,
});

PojosMetadataMap.create<StaffVo>(StaffVoSymbol, {
  id: Number,
  name: String,
  avatar: String,
  role: Number,
});

createMap<Staff, StaffListItem>(
  mapper, StaffSymbol, StaffListItemSymbol,
  forMember(d => d.role,   mapFrom(s => s.role ?? 0)),
  forMember(d => d.status, mapFrom(s => s.deletedAt !== null ? 0 : 1)),
);

createMap<Staff, StaffVo>(
  mapper, StaffSymbol, StaffVoSymbol,
  forMember(d => d.role, mapFrom(s => s.role ?? 0)),
);
