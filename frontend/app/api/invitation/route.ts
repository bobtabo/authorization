import { NextResponse } from "next/server";
import { randomBytes } from "crypto";

/**
 * モック: 本番では DB の app_settings.invitation_token を読み書きする想定。
 * dev ではプロセス内メモリのみ（サーバーレスでは揮発する点に注意）。
 */
let invitationToken = "x7R9pQ2wLmK4";

function generateToken(): string {
  const t = randomBytes(12).toString("hex").slice(0, 16);
  return t.length >= 8 ? t : `tok${randomBytes(8).toString("hex")}`;
}

function buildPublicUrl(token: string): string {
  const base =
    process.env.NEXT_PUBLIC_APP_URL?.replace(/\/$/, "") ??
    (process.env.VERCEL_URL ? `https://${process.env.VERCEL_URL}` : "http://localhost:3000");
  return `${base}/invitation/${token}`;
}

export async function GET() {
  const url = buildPublicUrl(invitationToken);
  return NextResponse.json({ url, token: invitationToken });
}

export async function POST() {
  invitationToken = generateToken();
  const url = buildPublicUrl(invitationToken);
  return NextResponse.json({ url, token: invitationToken });
}
