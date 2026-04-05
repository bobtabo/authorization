import path from "node:path";
import { fileURLToPath } from "node:url";
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");

  // Lambda ローカルサーバー経由でバックエンドへ転送する。
  // パスプレフィックス（/function/php 等）は Lambda 側でルーティングするため rewrite しない。
  const lambdaTarget = env.VITE_LAMBDA_PROXY_TARGET || "http://localhost:9000";

  const proxyEntries: Record<string, object> = {
    "/function": {
      target: lambdaTarget,
      changeOrigin: true,
      secure: false,
    },
  };

  return {
    envPrefix: ["VITE_", "POSTCODE_"],
    plugins: [react()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "."),
      },
    },
    server: {
      proxy: proxyEntries,
    },
  };
});
