/**
 * クライアントユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { createHash, generateKeyPairSync, randomBytes } from "crypto";
import { conflict, notFound } from "../../lib/errors.js";
import type { ClientRepository } from "../../domain/client/repository.js";
import type { Client } from "../../domain/client/entity.js";
import type { ClientListItem, ClientDetailVo, ClientStoreResultVo } from "../../domain/client/valueObjects.js";
import type { ClientStoreInput, ClientUpdateInput } from "./dto.js";

function writeSSHMPInt(val: bigint): Buffer {
  let hex = val.toString(16);
  if (hex.length % 2 !== 0) hex = "0" + hex;
  const bytes = Buffer.from(hex, "hex");
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

function toListItem(c: Client): ClientListItem {
  return {
    id: c.id, name: c.name, identifier: c.identifier, status: c.status ?? 1,
    startedAt: c.startedAt, stoppedAt: c.stoppedAt,
    createdAt: c.createdAt, updatedAt: c.updatedAt,
  };
}

function toDetailVo(c: Client): ClientDetailVo {
  return {
    id: c.id, name: c.name, identifier: c.identifier,
    postCode: c.postCode ?? "", pref: c.pref ?? "", city: c.city ?? "",
    address: c.address ?? "", building: c.building ?? "", tel: c.tel ?? "", email: c.email ?? "",
    status: c.status ?? 1, fingerprint: c.fingerprint ?? null,
    startedAt: c.startedAt, stoppedAt: c.stoppedAt,
    createdAt: c.createdAt, updatedAt: c.updatedAt,
  };
}

/** クライアントのユースケース実装。 */
export class ClientInteractor {
  constructor(private readonly repo: ClientRepository) {}

  /**
   * Bearer トークンでクライアントを認証します。
   * @param token - アクセストークン
   * @returns 認証成功の場合 true
   */
  async authenticateByToken(token: string): Promise<boolean> {
    return (await this.repo.findByToken(token)) !== undefined;
  }

  /**
   * 検索条件に合致するクライアント一覧の VO を返します。
   * @param keyword - キーワード検索
   * @param status - ステータスフィルター
   * @returns ClientListItem の配列
   */
  async getAllClients(keyword?: string, status?: number): Promise<ClientListItem[]> {
    const clients = await this.repo.findAll(keyword, status);
    return clients.map(toListItem);
  }

  /**
   * ID でクライアント詳細の VO を返します。
   * @param id - クライアントID
   * @returns ClientDetailVo
   * @throws AppError クライアントが存在しない場合
   */
  async getClientById(id: number): Promise<ClientDetailVo> {
    const c = await this.repo.findById(id);
    if (!c) throw notFound("client_not_found");
    return toDetailVo(c);
  }

  /**
   * クライアントを新規登録し、登録結果の VO を返します。RSA 鍵ペアを自動生成します。
   * @param data - 登録入力
   * @returns ClientStoreResultVo
   */
  async storeClient(data: ClientStoreInput): Promise<ClientStoreResultVo> {
    const identifier = randomBytes(8).toString("hex");

    const { privateKey, publicKey } = generateKeyPairSync("rsa", { modulusLength: 4096 });
    const fingerprint = rsaFingerprintFromKeyObject(publicKey);
    const privatePem = privateKey.export({ type: "pkcs1", format: "pem" }) as string;
    const publicPem = publicKey.export({ type: "spki", format: "pem" }) as string;
    const token = randomBytes(32).toString("hex");

    const executorId = data.executorId ?? 0;
    const saved = await this.repo.insert({
      name: data.name,
      identifier,
      postCode: data.postCode ?? null,
      pref: data.pref ?? null,
      city: data.city ?? null,
      address: data.address ?? null,
      building: data.building ?? null,
      tel: data.tel ?? null,
      email: data.email ?? null,
      token,
      publicKey: publicPem,
      privateKey: privatePem,
      fingerprint,
      status: 1,
      startedAt: null,
      stoppedAt: null,
      createdBy: executorId,
      updatedBy: executorId,
      deletedAt: null,
      deletedBy: null,
      version: 1,
    });
    return { id: saved.id, name: saved.name, identifier: saved.identifier, email: saved.email ?? "", token: saved.token ?? "" };
  }

  /**
   * クライアントを更新し、更新後の詳細 VO を返します。
   * @param id - クライアントID
   * @param data - 更新入力
   * @returns ClientDetailVo
   * @throws AppError クライアントが存在しない場合
   */
  async updateClientData(id: number, data: ClientUpdateInput): Promise<ClientDetailVo> {
    const client = await this.repo.findById(id);
    if (!client) throw notFound("client_not_found");

    const patch: Record<string, unknown> = { ...data };
    if (data.status !== undefined && data.status !== client.status) {
      const now = new Date();
      if (data.status === 2) patch.startedAt = now;
      else if (data.status === 3) patch.stoppedAt = now;
    }
    await this.repo.update(id, patch, client.version);
    const updated = await this.repo.findById(id);
    return toDetailVo(updated!);
  }

  /**
   * クライアントをステータス Closed(4) に変更してから論理削除します。
   * @param id - クライアントID
   * @throws AppError クライアントが存在しない場合
   */
  async destroyClient(id: number): Promise<void> {
    const client = await this.repo.findById(id);
    if (!client) throw notFound("client_not_found");
    await this.repo.update(id, { status: 4 }, client.version);
    await this.repo.softDelete(id, client.version + 1);
  }
}
