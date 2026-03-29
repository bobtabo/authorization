import { defineConfig, devices } from "@playwright/test";

/**
 * E2E は専用ポートで起動し、手元の `npm run dev`（5173）と干渉しないようにする。
 * VITE_E2E=1 でログイン画面の「Googleで続行」がモック遷移する。
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
    baseURL: "http://127.0.0.1:5174",
    trace: "on-first-retry",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: {
    command: "npm run dev -- --port 5174 --strictPort --host 127.0.0.1",
    url: "http://127.0.0.1:5174",
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    env: { ...process.env, VITE_E2E: "1" },
  },
});
