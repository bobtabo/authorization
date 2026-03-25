
  # Authorization UI Flow

  This is a code bundle for Authorization UI Flow. The original project is available at https://www.figma.com/design/poZ4dOYtKiRQS54ThDG8hr/Authorization-UI-Flow.

  ## Running the code

  Run `npm i` to install the dependencies.

  Run `npm run dev` to start the development server.

  ## 画面キャプチャ（`src/assets/flow/`）

  SVG は **`src/assets/flow/*.svg`** に置き、`src/app/flowImages.ts` で `?url` import しています（Vite が dev / 本番とも URL を解決するので表示が安定します）。PNG に差し替えるときは同じパスに `.png` を置き、`flowImages.ts` の import を `../assets/flow/login.png?url` のように変更してください。
