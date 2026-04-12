import { createHash, generateKeyPairSync } from "crypto";
import { randomBytes } from "crypto";
import { conflict, notFound } from "../lib/errors.js";
import {
  findAllClients, findClientById, findClientByToken, findClientByIdentifier,
  insertClient, updateClient, softDeleteClient,
} from "../repositories/clientRepo.js";
import type { Client } from "../db/schema.js";

function rsaFingerprint(publicKeyDer: Buffer): string {
  // SSH wire format: ssh-rsa | exponent | modulus
  // Node's generateKeyPairSync gives us the key object — re-export as DER to extract n/e
  // 簡易実装: publicKey の DER から n/e を取り出すのは複雑なため、
  // crypto.createPublicKey で KeyObject を使って spki DER → parseASN1 で指数・係数を抽出する
  // ここでは jose/forge の代わりに node の KeyObject を使う
  throw new Error("use rsaFingerprintFromPem instead");
}

function writeSSHMPInt(val: bigint): Buffer {
  let hex = val.toString(16);
  if (hex.length % 2 !== 0) hex = "0" + hex;
  const bytes = Buffer.from(hex, "hex");
  // SSH MPI: 先頭ビットが1なら0x00を付加
  const needPad = bytes[0]! & 0x80 ? Buffer.from([0x00]) : Buffer.alloc(0);
  const data = Buffer.concat([needPad, bytes]);
  const len = Buffer.allocUnsafe(4);
  len.writeUInt32BE(data.length, 0);
  return Buffer.concat([len, data]);
}

function writeSSHStr(s: string): Buffer {
  const b = Buffer.from(s);
  const len = Buffer.allocUnsafe(4);
  len.writeUInt32BE(b.length, 0);
  return Buffer.concat([len, b]);
}

function rsaFingerprintFromKeyObject(pubKey: ReturnType<typeof generateKeyPairSync>["publicKey"]): string {
  // jwk から n/e を取得
  const jwk = pubKey.export({ format: "jwk" }) as { n: string; e: string };
  const nBytes = Buffer.from(jwk.n, "base64url");
  const eBytes = Buffer.from(jwk.e, "base64url");

  const wire = Buffer.concat([
    writeSSHStr("ssh-rsa"),
    writeSSHMPInt(BigInt("0x" + eBytes.toString("hex"))),
    writeSSHMPInt(BigInt("0x" + nBytes.toString("hex"))),
  ]);

  const hash = createHash("sha256").update(wire).digest();
  const b64 = hash.toString("base64").replace(/=+$/, "");
  return `SHA256:${b64}`;
}

export function staffStatus(staff: { deletedAt: Date | null }): number {
  return staff.deletedAt ? 0 : 1;
}

export async function authenticateByToken(token: string): Promise<Client | undefined> {
  return findClientByToken(token);
}

export async function getAllClients(keyword?: string, status?: number): Promise<Client[]> {
  return findAllClients(keyword, status);
}

export async function getClientById(id: number): Promise<Client> {
  const c = await findClientById(id);
  if (!c) throw notFound("client_not_found");
  return c;
}

export async function storeClient(data: {
  name: string; identifier: string; postCode?: string; pref?: string;
  city?: string; address?: string; building?: string; tel?: string; email?: string;
}): Promise<Client> {
  const existing = await findClientByIdentifier(data.identifier);
  if (existing) throw conflict("identifier_already_exists");

  const { privateKey, publicKey } = generateKeyPairSync("rsa", { modulusLength: 4096 });
  const fingerprint = rsaFingerprintFromKeyObject(publicKey);
  const privatePem = privateKey.export({ type: "pkcs1", format: "pem" }) as string;
  const publicPem = publicKey.export({ type: "spki", format: "pem" }) as string;
  const token = randomBytes(32).toString("hex");

  return insertClient({
    ...data,
    token,
    publicKey: publicPem,
    privateKey: privatePem,
    fingerprint,
    status: 0,
  });
}

export async function updateClientData(id: number, data: {
  name?: string; postCode?: string; pref?: string; city?: string;
  address?: string; building?: string; tel?: string; email?: string; status?: number;
}): Promise<Client> {
  const client = await getClientById(id);
  const patch: Record<string, unknown> = { ...data };

  if (data.status !== undefined && data.status !== client.status) {
    const now = new Date();
    if (data.status === 1) patch.startedAt = now;
    else if (data.status === 2) patch.stoppedAt = now;
  }

  await updateClient(id, patch);
  return getClientById(id);
}

export async function destroyClient(id: number): Promise<void> {
  await getClientById(id);
  await updateClient(id, { status: 4 });
  await softDeleteClient(id);
}
