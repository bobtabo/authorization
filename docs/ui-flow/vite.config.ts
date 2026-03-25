import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    // SVG を data: URL に埋め込まずファイルとして出す（<img> + data:image/svg+xml は環境によって描画されないことがある）
    assetsInlineLimit: 0,
  },
});
