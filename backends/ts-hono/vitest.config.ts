import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    // テスト用 DB に向ける（dotenv/config より先に設定されるため上書きされない）
    env: {
      DB_DATABASE: "authorization_test",
    },
    setupFiles: ["./src/test/setup.ts"],
    // テストは順番に実行（DB 共有のため並列不可）
    pool: "forks",
    poolOptions: { forks: { singleFork: true } },
  },
});
