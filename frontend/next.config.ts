import type { NextConfig } from "next";
import path from "path";

/**
 * リポジトリ直下に別の lockfile があると Turbopack が誤ったルートを採用し、
 * dev で不安定になることがあるため、この frontend ディレクトリを明示する。
 */

// API Gateway エミュレータ経由でバックエンドへ転送する。
// パスプレフィックス（/function/php 等）は Lambda 側でルーティングするため rewrite しない。
const lambdaTarget = process.env.LAMBDA_PROXY_TARGET || "http://localhost:8080";

const nextConfig: NextConfig = {
  turbopack: {
    root: path.join(__dirname),
  },
  async rewrites() {
    return [
      {
        source: "/function/:path*",
        destination: `${lambdaTarget}/function/:path*`,
      },
    ];
  },
};

export default nextConfig;
