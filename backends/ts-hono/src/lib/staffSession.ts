/**
 * staff_id クッキーの HMAC 署名・検証モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { createHmac, timingSafeEqual } from "node:crypto";

/**
 * staff_id を署名します。フォーマットは "{staffId}.{expiresAtUnixSeconds}.{署名}"。
 * @param staffId - スタッフID
 * @param secret - 署名シークレット
 * @param lifetimeSeconds - 有効期間（秒）
 * @returns 署名済みの値
 */
export function signStaffId(staffId: number, secret: string, lifetimeSeconds: number): string {
  const expiresAt = Math.floor(Date.now() / 1000) + lifetimeSeconds;
  const payload = `${staffId}.${expiresAt}`;
  return `${payload}.${hmacHex(payload, secret)}`;
}

/**
 * 署名済みの staff_id を検証します。
 * @param value - クッキーの値
 * @param secret - 署名シークレット
 * @returns 検証済みのスタッフID（不正または期限切れの場合は 0）
 */
export function verifyStaffId(value: string | undefined | null, secret: string): number {
  if (!value) return 0;
  const parts = value.split(".");
  if (parts.length !== 3) return 0;
  const [idPart, expPart, sig] = parts;
  if (!idPart || !expPart || !sig) return 0;

  const expectedSig = hmacHex(`${idPart}.${expPart}`, secret);
  const a = Buffer.from(sig);
  const b = Buffer.from(expectedSig);
  if (a.length !== b.length || !timingSafeEqual(a, b)) return 0;

  const id = parseInt(idPart, 10);
  const expiresAt = parseInt(expPart, 10);
  if (isNaN(id) || isNaN(expiresAt)) return 0;
  if (Math.floor(Date.now() / 1000) > expiresAt) return 0;

  return id;
}

function hmacHex(payload: string, secret: string): string {
  return createHmac("sha256", secret).update(payload).digest("hex");
}
