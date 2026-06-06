/**
 * クライアントユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { createHash, generateKeyPairSync, randomBytes } from "crypto";
import { conflict, internal, notFound } from "../../lib/errors.js";
import type { ClientRepository } from "../../domain/client/repository.js";
import type { ClientListItem, ClientDetailVo, ClientStoreResultVo, ClientQrVo, ClientInfoVo, ClientStartVo } from "../../domain/client/valueObjects.js";
import type { ClientStoreInput, ClientUpdateInput } from "./dto.js";
import { mapper } from "../../support/mapper.js";
import { ClientSymbol, ClientListItemSymbol, ClientDetailVoSymbol, ClientStoreResultVoSymbol } from "../../support/mappers/index.js";

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
   * @param options - ページングオプション
   * @returns ClientListItem の配列と総件数のタプル
   */
  async getAllClients(
    keyword?: string,
    status?: number,
    options?: { offset?: number; limit?: number; sort?: string; sortType?: string },
  ): Promise<{ items: ClientListItem[]; count: number }> {
    const count = await this.repo.countAll(keyword, status);
    const clients = await this.repo.findAll(keyword, status, options);
    return { items: mapper.mapArray(clients, ClientSymbol, ClientListItemSymbol), count };
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
    return mapper.map(c, ClientSymbol, ClientDetailVoSymbol);
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
    return mapper.map(saved, ClientSymbol, ClientStoreResultVoSymbol);
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
    return mapper.map(updated!, ClientSymbol, ClientDetailVoSymbol);
  }

  /**
   * identifier で QRコードデータを返します。
   * @param identifier - クライアント識別子
   * @returns ClientQrVo
   * @throws AppError クライアントが存在しない場合
   */
  async getQr(identifier: string): Promise<ClientQrVo> {
    const client = await this.repo.findByIdentifier(identifier);
    if (!client) throw notFound("client_not_found");
    return {
      identifier: client.identifier,
      deeplinkUrl: `authgateway://clients/${client.identifier}/info`,
    };
  }

  /**
   * identifier でスマホアプリ向けクライアント情報を返します。
   * @param identifier - クライアント識別子
   * @returns ClientInfoVo
   * @throws AppError クライアントが存在しない場合
   */
  async getInfo(identifier: string): Promise<ClientInfoVo> {
    const client = await this.repo.findByIdentifier(identifier);
    if (!client) throw notFound("client_not_found");
    return {
      identifier: client.identifier,
      name: client.name,
      status: client.status ?? 0,
    };
  }

  /**
   * 利用開始処理を行い、アクセストークンを返します。
   * Active 以外なら Active(2) に遷移し、start_at が未設定なら now をセット、stop_at をクリアします。
   * 既に Active でもアクセストークンを返します。
   * @param identifier - クライアント識別子
   * @returns ClientStartVo
   * @throws AppError クライアントが存在しない場合
   */
  async startClient(identifier: string): Promise<ClientStartVo> {
    const client = await this.repo.findByIdentifier(identifier);
    if (!client) throw notFound("client_not_found");

    if (client.status !== 2) {
      const now = new Date();
      await this.repo.update(client.id, {
        status: 2,
        startedAt: client.startedAt ?? now,
        stoppedAt: null,
      }, client.version);
    }

    const updated = await this.repo.findByIdentifier(identifier);
    if (!updated?.token) throw internal("token_missing");
    return { accessToken: updated.token };
  }

  /**
   * 利用停止処理を行います。
   * Active(2) なら Suspended(3) に遷移し、stop_at に now をセットします。
   * Active 以外は何もしません。
   * @param identifier - クライアント識別子
   * @throws AppError クライアントが存在しない場合
   */
  async stopClient(identifier: string): Promise<void> {
    const client = await this.repo.findByIdentifier(identifier);
    if (!client) throw notFound("client_not_found");

    if (client.status === 2) {
      await this.repo.update(client.id, {
        status: 3,
        stoppedAt: new Date(),
      }, client.version);
    }
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
