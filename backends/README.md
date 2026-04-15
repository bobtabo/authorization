# バックエンド構成

> 💡 **マイクロサービス分割ではありません。**  
> 認可サーバーのバックエンドを、**複数の言語・フレームワーク**で構成しています。  
> 同一の OpenAPI に沿った実装を、ディレクトリごとに分けています（実験やプロトタイプ専用ではありません）。

📖 **API の正:** [`docs/api-spec/openapi.yml`](../docs/api-spec/openapi.yml) ／ Swagger は [`docs/api-spec`](../docs/api-spec/README.md)

---

## 📂 実装スタック一覧

<!-- ロゴは各公式（Go は go.dev ブランド PNG 等）／ devicons（jsDelivr）。ドキュメント列は各ディレクトリの README.md。 -->

| | スタック | ディレクトリ | ドキュメント | 状態 |
|:---:|:---|:---|:---|:---:|
| <img src="https://go.dev/blog/go-brand/Go-Logo/PNG/Go-Logo_Blue.png" width="45" height="40" alt="Go" title="Go" /> <img src="https://raw.githubusercontent.com/gin-gonic/logo/master/color.png" width="44" height="44" alt="Gin" title="Gin" /> | [**Go**](https://go.dev/) + [**Gin**](https://gin-gonic.com/) | [`go-gin/`](./go-gin/) | [README.md](./go-gin/README.md) | ✅ 完了 |
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/php/php-original.svg" width="32" height="32" alt="PHP" /> <img src="https://raw.githubusercontent.com/laravel/art/master/logo-mark/5%20svg/3%20rgb/1%20Full%20Color/laravel-mark-rgb-red.svg" width="32" height="32" alt="Laravel" /> | [**PHP**](https://www.php.net/) + [**Laravel**](https://laravel.com/) | [`php-laravel/`](./php-laravel/) | [README.md](./php-laravel/README.md) | ✅ 完了 |
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/python/python-original.svg" width="32" height="32" alt="Python" /> <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/fastapi/fastapi-original.svg" width="32" height="32" alt="FastAPI" /> | [**Python**](https://www.python.org/) + [**FastAPI**](https://fastapi.tiangolo.com/) | [`python-fastapi/`](./python-fastapi/) | [README.md](./python-fastapi/README.md) | ✅ 完了 |
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/typescript/typescript-original.svg" width="32" height="32" alt="TypeScript" /> <img src="https://hono.dev/images/logo.png" width="36" height="36" alt="Hono" /> | [**TypeScript**](https://www.typescriptlang.org/) + [**Hono**](https://hono.dev/) | [`ts-hono/`](./ts-hono/) | [README.md](./ts-hono/README.md) | ✅ 完了 |
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/rust/rust-original.svg" width="32" height="32" alt="Rust" /> | [**Rust**](https://www.rust-lang.org/) + 検討中 | | | 🚧 予定 |
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/kotlin/kotlin-original.svg" width="32" height="32" alt="Kotlin" /> | [**Kotlin**](https://kotlinlang.org/) + 検討中 | | | 🚧 予定 |
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/ruby/ruby-original.svg" width="32" height="32" alt="Ruby" /> | [**Ruby**](https://www.ruby-lang.org/) + 検討中 | | | 🚧 予定 |

### 凡例

| 記号 | 意味 |
|:---:|:---|
| ✅ | 完了 |
| 🚧 | 予定（未着手）／開発中 |

---

## 🔗 クイックリンク

| 絵文字 | リンク |
|:---:|:---|
| 📘 | [OpenAPI（YAML）](../docs/api-spec/openapi.yml) |
| 🖥️ | [Swagger / docker-compose](../docs/api-spec/README.md) |
| 🏠 | [リポジトリルート README](../README.md) |

各バックエンドの **起動・テスト・環境変数** は、上の表のディレクトリ内 README を参照してください。
