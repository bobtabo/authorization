
  # Authorization UI Flow

  This is a code bundle for Authorization UI Flow. The original project is available at https://www.figma.com/design/poZ4dOYtKiRQS54ThDG8hr/Authorization-UI-Flow.

  ## Running the code

  Run `npm i` to install the dependencies.

  Run `npm run dev` to start the development server.

  ## 画面キャプチャ（`public/flow/`）

  SVG は **`public/flow/*.svg`** に置きます。`src/app/flowImages.ts` は `import.meta.env.BASE_URL` と組み合わせたパス（例: `/flow/login.svg`）を参照します。差し替えは同じファイル名で上書きしてください。PNG にする場合は `public/flow/` に置き、`flowImages.ts` の拡張子を合わせてください。
