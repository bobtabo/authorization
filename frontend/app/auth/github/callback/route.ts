import { type NextRequest, NextResponse } from "next/server";

/**
 * GitHub OAuth コールバックのディスパッチャー。
 *
 * GitHub は redirect_uri を1つしか登録できないため、このルートが受け取り、
 * state の先頭セグメント（"{runtime}|..." の runtime 部分）を見て
 * /function/{runtime}/auth/github/callback へ転送する。
 *
 * state フォーマット:
 *   通常ログイン: "{runtime}"              例: "go-gin"
 *   招待ログイン: "{runtime}|{token}"      例: "go-gin|abc123..."
 */
export async function GET(request: NextRequest): Promise<NextResponse> {
  const { searchParams, origin } = new URL(request.url);
  const code  = searchParams.get("code")  ?? "";
  const state = searchParams.get("state") ?? "";

  const runtime = state.split("|")[0];
  if (!runtime) {
    return NextResponse.redirect(new URL("/error?code=400", origin));
  }

  const dest = new URL(`/function/${runtime}/auth/github/callback`, origin);
  if (code)  dest.searchParams.set("code",  code);
  if (state) dest.searchParams.set("state", state);

  return NextResponse.redirect(dest);
}
