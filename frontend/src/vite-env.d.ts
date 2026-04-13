/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string;
  /** dev: `/function` のプロキシ先（Lambda 等） */
  readonly VITE_LAMBDA_PROXY_TARGET?: string;
  /** 互換: バックエンド直プロキシ先 */
  readonly VITE_API_PROXY_TARGET?: string;
  /** Postcode JP（vite の envPrefix に POSTCODE_ を含める） */
  readonly POSTCODE_API_KEY: string;
  /** Playwright E2E 時のみ: ログイン画面からコンソールへ遷移するモック */
  readonly VITE_E2E?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
