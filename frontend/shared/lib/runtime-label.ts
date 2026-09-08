/** バックエンドランタイム識別子（`RUNTIME_STORAGE_KEY` の値）から表示用ラベルへのマップ。 */
export const RUNTIME_LABEL: Record<string, string | undefined> = {
  csharp: "C#",
  "go-gin": "Go (Gin)",
  "go-beego": "Go (Beego)",
  "go-echo": "Go (Echo)",
  kotlin: "Kotlin",
  php: "PHP",
  python: "Python",
  "rb-hanami": "Ruby (Hanami)",
  "rb-rails": "Ruby (Rails)",
  rust: "Rust",
  ts: "TypeScript",
};
