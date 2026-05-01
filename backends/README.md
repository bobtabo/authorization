# バックエンド構成

> 💡 **マイクロサービス分割ではありません。**  
> 認可サーバーのバックエンドを、**複数の言語・フレームワーク**で構成しています。  
> 同一の OpenAPI に沿った実装を、ディレクトリごとに分けています（実験やプロトタイプ専用ではありません）。

📖 **API の正:** [`docs/api-spec/openapi.yml`](../docs/api-spec/openapi.yml) ／ Swagger は [`docs/api-spec`](../docs/api-spec/README.md)

---

## 📂 実装スタック一覧

<!-- ロゴは各公式（Go は go.dev ブランド PNG 等）／ devicons（jsDelivr）。ドキュメント列は各ディレクトリの README.md。 -->

<table>
<thead>
<tr>
<th align="center"></th>
<th>スタック</th>
<th>ディレクトリ</th>
<th>ドキュメント</th>
<th align="center">状態</th>
</tr>
</thead>
<tbody>
<tr>
<td align="center"><img src="https://go.dev/blog/go-brand/Go-Logo/PNG/Go-Logo_Blue.png" width="45" height="40" alt="Go"></td>
<td><a href="https://go.dev/"><b>Go</b></a> + <a href="https://gin-gonic.com/"><b>Gin</b></a></td>
<td><a href="./go-gin/"><code>go-gin/</code></a></td>
<td><a href="./go-gin/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://go.dev/blog/go-brand/Go-Logo/PNG/Go-Logo_Blue.png" width="45" height="40" alt="Go"></td>
<td><a href="https://go.dev/"><b>Go</b></a> + <a href="https://beego.vip/"><b>Beego</b></a></td>
<td><a href="./go-beego/"><code>go-beego/</code></a></td>
<td><a href="./go-beego/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://go.dev/blog/go-brand/Go-Logo/PNG/Go-Logo_Blue.png" width="45" height="40" alt="Go"></td>
<td><a href="https://go.dev/"><b>Go</b></a> + <a href="https://echo.labstack.com/"><b>Echo</b></a></td>
<td><a href="./go-echo/"><code>go-echo/</code></a></td>
<td><a href="./go-echo/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/php/php-original.svg" width="32" height="32" alt="PHP"></td>
<td><a href="https://www.php.net/"><b>PHP</b></a> + <a href="https://laravel.com/"><b>Laravel</b></a></td>
<td><a href="./php-laravel/"><code>php-laravel/</code></a></td>
<td><a href="./php-laravel/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/python/python-original.svg" width="32" height="32" alt="Python"></td>
<td><a href="https://www.python.org/"><b>Python</b></a> + <a href="https://fastapi.tiangolo.com/"><b>FastAPI</b></a></td>
<td><a href="./python-fastapi/"><code>python-fastapi/</code></a></td>
<td><a href="./python-fastapi/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/typescript/typescript-original.svg" width="32" height="32" alt="TypeScript"></td>
<td><a href="https://www.typescriptlang.org/"><b>TypeScript</b></a> + <a href="https://hono.dev/"><b>Hono</b></a></td>
<td><a href="./ts-hono/"><code>ts-hono/</code></a></td>
<td><a href="./ts-hono/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/rust/rust-original.svg" width="32" height="32" alt="Rust"></td>
<td><a href="https://www.rust-lang.org/"><b>Rust</b></a> + <a href="https://github.com/tokio-rs/axum"><b>Axum</b></a></td>
<td><a href="./rust-axum/"><code>rust-axum/</code></a></td>
<td><a href="./rust-axum/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/kotlin/kotlin-original.svg" width="32" height="32" alt="Kotlin"></td>
<td><a href="https://kotlinlang.org/"><b>Kotlin</b></a> + <a href="https://ktor.io/"><b>Ktor</b></a></td>
<td><a href="./kotlin-ktor/"><code>kotlin-ktor/</code></a></td>
<td><a href="./kotlin-ktor/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/ruby/ruby-original.svg" width="32" height="32" alt="Ruby"></td>
<td><a href="https://www.ruby-lang.org/"><b>Ruby</b></a> + <a href="https://rubyonrails.org/"><b>Ruby on Rails</b></a></td>
<td><a href="./ruby-rails/"><code>ruby-rails/</code></a></td>
<td><a href="./ruby-rails/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/ruby/ruby-original.svg" width="32" height="32" alt="Ruby"></td>
<td><a href="https://www.ruby-lang.org/"><b>Ruby</b></a> + <a href="https://hanamirb.org/"><b>Hanami</b></a></td>
<td><a href="./ruby-hanami/"><code>ruby-hanami/</code></a></td>
<td><a href="./ruby-hanami/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
</tbody>
</table>

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
