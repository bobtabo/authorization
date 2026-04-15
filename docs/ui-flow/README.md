<p align="center">
<a href="https://react.dev/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/react/react-original-wordmark.svg" height="72" alt="React"></a>
&nbsp;&nbsp;
<a href="https://www.typescriptlang.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/typescript/typescript-original.svg" height="72" alt="TypeScript"></a>
&nbsp;&nbsp;
<a href="https://tailwindcss.com/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/tailwindcss/tailwindcss-original.svg" height="72" alt="Tailwind CSS"></a>
&nbsp;&nbsp;
<a href="https://vite.dev/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/vitejs/vitejs-original.svg" height="72" alt="Vite"></a>
&nbsp;&nbsp;
<a href="https://vercel.com/" target="_blank"><img src="https://encore.cloud/assets/resources/vercel_cover.jpg" height="72" alt="Vercel"></a>
</p>

<p align="center">
<a href="https://react.dev/"><img src="https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white" alt="React 19"></a>
<a href="https://www.typescriptlang.org/"><img src="https://img.shields.io/badge/TypeScript-5.7-3178C6?logo=typescript&logoColor=white" alt="TypeScript 5.7"></a>
<a href="https://tailwindcss.com/"><img src="https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?logo=tailwindcss&logoColor=white" alt="Tailwind CSS 4"></a>
<a href="https://vite.dev/"><img src="https://img.shields.io/badge/Vite-6-646CFF?logo=vite&logoColor=white" alt="Vite 6"></a>
<a href="https://vercel.com/"><img src="https://img.shields.io/badge/Vercel-000000?logo=vercel&logoColor=white" alt="Vercel"></a>
</p>

---

## :book: 概要

認可サーバーの **UI フロー閲覧ツール** です。

Figma で設計した画面フローを Web アプリとして確認できます。  
Vercel にデプロイして利用します。

> 元の Figma プロジェクト: [Authorization UI Flow](https://www.figma.com/design/poZ4dOYtKiRQS54ThDG8hr/Authorization-UI-Flow)

---

## :file_folder: ディレクトリ構成

```
docs/ui-flow/
├── src/
│   ├── app/            # ページコンポーネント
│   │   └── flowImages.ts   # 画像パス定義
│   ├── styles/         # グローバル CSS
│   └── main.tsx        # エントリーポイント
├── public/
│   └── flow/           # 画面キャプチャ画像（*.png）
├── vite.config.ts
└── vercel.json         # Vercel 設定
```

---

## :rocket: セットアップ・起動

### 1. 依存パッケージのインストール

```bash
npm install
```

### 2. 開発サーバー起動

```bash
npm run dev
```

---

## :camera: 画面キャプチャの差し替え

画像は **`public/flow/*.png`** に配置します。  
差し替えは同じファイル名で上書きしてください。

---

## :cloud: デプロイ（Vercel）

Vercel に接続済みのリポジトリへプッシュすると自動デプロイされます。
