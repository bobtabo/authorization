import { createPrivateKey, createPublicKey } from "crypto";
import { SignJWT, jwtVerify, importPKCS8, importSPKI } from "jose";
import { config } from "../config.js";
import { unauthorized, notFound, internal } from "../lib/errors.js";
import { findClientByToken, findClientByIdentifier } from "../repositories/clientRepo.js";
import { getJwt, putJwt } from "../repositories/gateCacheRepo.js";

export async function issueToken(accessToken: string, member: string): Promise<string> {
  const client = await findClientByToken(accessToken);
  if (!client) throw unauthorized("invalid_token");
  if (!client.privateKey) throw internal("private_key_not_found");

  const cached = await getJwt(client.identifier, member);
  if (cached) return cached;

  const token = await issueJwt(client.privateKey, client.identifier, member);
  await putJwt(client.identifier, member, token);
  return token;
}

async function issueJwt(privateKeyPem: string, identifier: string, member: string): Promise<string> {
  const privateKey = await importPKCS8(privateKeyPem, config.jwt.algorithm);
  const now = Math.floor(Date.now() / 1000);
  return new SignJWT({ sub: member, aud: [identifier] })
    .setProtectedHeader({ alg: config.jwt.algorithm })
    .setIssuer(config.jwt.issuer)
    .setIssuedAt(now)
    .setExpirationTime(now + config.jwt.ttl)
    .sign(privateKey);
}

export async function verify(identifier: string, token: string): Promise<Record<string, unknown>> {
  const client = await findClientByIdentifier(identifier);
  if (!client) throw notFound("client_not_found");
  if (!client.publicKey) throw internal("public_key_not_found");

  try {
    const publicKey = await importSPKI(client.publicKey, config.jwt.algorithm);
    const { payload } = await jwtVerify(token, publicKey, {
      issuer: config.jwt.issuer,
      audience: identifier,
      algorithms: [config.jwt.algorithm],
    });
    return {
      identifier,
      member: payload.sub,
      fingerprint: client.fingerprint,
      payload,
    };
  } catch (e) {
    throw unauthorized(e instanceof Error ? e.message : "jwt_error");
  }
}
