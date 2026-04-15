import { defineConfig, devices } from "@playwright/test";
import os from "os";
import path from "path";

/**
 * E2E は専用ポートで起動し、手元の `npm run dev`（3000）と干渉しないようにする。
 * NEXT_PUBLIC_E2E=1 でログイン画面の「Googleで続行」がモック遷移する。
 *
 * ブラウザが見つからない場合: `PLAYWRIGHT_BROWSERS_PATH` が壊れたキャッシュを指していることがある。
 * そのときは `env -u PLAYWRIGHT_BROWSERS_PATH npx playwright install chromium` のあと再実行。
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  reporter: [["list"]],
  use: {
    baseURL: "http://127.0.0.1:3001",
    trace: "on-first-retry",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
    {
      // 撮影用: 本物の Chrome + Retina + ライトモード（PHP バックエンドのみ）
      // 実行: npx playwright test --project=screenshot
      name: "screenshot",
      grep: /\[PHP\]/,
      outputDir: path.join(os.homedir(), "Downloads", "playwright-screenshots"),
      use: {
        ...devices["Desktop Chrome"],
        channel: "chrome",
        colorScheme: "light",
        deviceScaleFactor: 2,
        viewport: { width: 1280, height: 800 },
        screenshot: { mode: "on", fullPage: true },
      },
    },
  ],
  webServer: {
    // dev サーバーと共存できるよう、E2E 専用に build → start する。
    // NEXT_PUBLIC_E2E=1 はビルド時にインライン化されるため build にも渡す。
    command: "NEXT_PUBLIC_E2E=1 npm run build && npm run start -- -p 3001 -H 127.0.0.1",
    url: "http://127.0.0.1:3001",
    reuseExistingServer: !process.env.CI,
    timeout: 300_000,
    env: { ...process.env, NEXT_PUBLIC_E2E: "1" },
  },
});
