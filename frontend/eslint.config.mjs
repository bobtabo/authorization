import { existsSync, readdirSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

import js from "@eslint/js";
import globals from "globals";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import unusedImports from "eslint-plugin-unused-imports";
import importPlugin from "eslint-plugin-import";
import tseslint from "typescript-eslint";
import { defineConfig, globalIgnores } from "eslint/config";

const rootDir = dirname(fileURLToPath(import.meta.url));

// features/ 直下のディレクトリ一覧（feature 追加時に設定を触らなくても自動で対象になる）
const featuresDir = join(rootDir, "features");
const featureNames = existsSync(featuresDir)
  ? readdirSync(featuresDir, { withFileTypes: true })
      .filter((entry) => entry.isDirectory())
      .map((entry) => entry.name)
  : [];

export default defineConfig([
  globalIgnores(["dist", ".next", "test-results", "playwright-report"]),
  {
    files: ["**/*.{ts,tsx}"],
    ignores: ["e2e/**"],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs["recommended-latest"],
      reactRefresh.configs.recommended,
    ],
    plugins: {
      "unused-imports": unusedImports,
      import: importPlugin,
    },
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    settings: {
      // tsconfig の paths（@/*）を解決し、import/order で internal として正しく分類させる
      "import/resolver": {
        typescript: true,
        node: true,
      },
    },
    rules: {
      // Prettier は導入せず、eslint --fix で未使用 import の削除と import の並び替えを行う
      "@typescript-eslint/no-unused-vars": "off",
      "unused-imports/no-unused-imports": "error",
      "unused-imports/no-unused-vars": [
        "warn",
        {
          vars: "all",
          varsIgnorePattern: "^_",
          args: "after-used",
          argsIgnorePattern: "^_",
        },
      ],
      "import/order": [
        "error",
        {
          groups: [
            "builtin",
            "external",
            "internal",
            "parent",
            "sibling",
            "index",
          ],
          "newlines-between": "always",
          alphabetize: { order: "asc", caseInsensitive: true },
        },
      ],
      // Next.js の `layout.tsx`/`page.tsx` は慣習的にコンポーネントと定数（`metadata` 等）を
      // 同居させる。React Context ファイルも Provider と定数・hook を同居させるのが一般的なため、
      // 定数エクスポートは Fast Refresh 対象外として許容する。
      "react-refresh/only-export-components": ["warn", { allowConstantExport: true }],
    },
  },
  {
    // feature 間の直接 import 禁止（frontend/AGENTS.md）。
    // 各 feature は自分自身と shared/ のみ参照でき、他 feature を import するとエラーになる。
    // 解決後のパスで判定するため `@/features/x` と `../../x` のどちらの書き方でも検出される。
    files: ["features/**/*.{ts,tsx}"],
    rules: {
      "import/no-restricted-paths": [
        "error",
        {
          basePath: featuresDir,
          zones: featureNames.map((name) => ({
            target: `./${name}`,
            from: ".",
            except: [`./${name}`],
            message: `feature 間の直接 import は禁止です。共有したいものは shared/ に移動してください（features/${name} からは自分自身と @/shared/** のみ参照できます）。`,
          })),
        },
      ],
    },
  },
  {
    // tailwind.config.ts はプラグイン読み込みに CommonJS の require を使うのが標準的な書き方のため、
    // このファイルに限り no-require-imports を無効化する。
    files: ["tailwind.config.ts"],
    rules: {
      "@typescript-eslint/no-require-imports": "off",
    },
  },
  {
    files: ["e2e/**/*.ts", "playwright.config.ts"],
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    languageOptions: {
      globals: globals.node,
    },
    rules: {
      // Playwright の fixture 定義（`test.extend`）は `({}, use, testInfo) => {...}` のように
      // 未使用の第1引数を空分割代入で受けるのが標準的な書き方のため無効化する。
      "no-empty-pattern": "off",
    },
  },
]);
