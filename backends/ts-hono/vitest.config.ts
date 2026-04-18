import { defineConfig } from "vitest/config";
import { config as loadDotenv } from "dotenv";

// ローカル優先（host.docker.internal など）、なければ testing（CI 用: 127.0.0.1）
loadDotenv({ path: ".env.testing.local" });
loadDotenv({ path: ".env.testing" });

export default defineConfig({
  test: {
    // dotenv より後に設定されるため上書き保証
    env: {
      DB_DATABASE: "authorization_test",
    },
    setupFiles: ["./src/test/setup.ts"],
    // テストは順番に実行（DB 共有のため並列不可）
    pool: "forks",
    poolOptions: { forks: { singleFork: true } },
  },
});
