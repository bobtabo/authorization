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
<td align="center"><img src="https://go.dev/blog/go-brand/Go-Logo/PNG/Go-Logo_Blue.png" width="45" height="40" alt="Go"> <img src="https://raw.githubusercontent.com/gin-gonic/logo/master/color.png" width="44" height="44" alt="Gin"></td>
<td><a href="https://go.dev/"><b>Go</b></a> + <a href="https://gin-gonic.com/"><b>Gin</b></a></td>
<td><a href="./go-gin/"><code>go-gin/</code></a></td>
<td><a href="./go-gin/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://go.dev/blog/go-brand/Go-Logo/PNG/Go-Logo_Blue.png" width="45" height="40" alt="Go"> <img src="https://avatars.githubusercontent.com/u/3884041" width="44" height="44" alt="Beego"></td>
<td><a href="https://go.dev/"><b>Go</b></a> + <a href="https://beego.vip/"><b>Beego</b></a></td>
<td><a href="./go-beego/"><code>go-beego/</code></a></td>
<td><a href="./go-beego/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://go.dev/blog/go-brand/Go-Logo/PNG/Go-Logo_Blue.png" width="45" height="40" alt="Go"> <img src="https://avatars.githubusercontent.com/u/2624634" width="44" height="44" alt="Echo"></td>
<td><a href="https://go.dev/"><b>Go</b></a> + <a href="https://echo.labstack.com/"><b>Echo</b></a></td>
<td><a href="./go-echo/"><code>go-echo/</code></a></td>
<td><a href="./go-echo/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/php/php-original.svg" width="32" height="32" alt="PHP"> <img src="https://raw.githubusercontent.com/laravel/art/master/logo-mark/5%20svg/3%20rgb/1%20Full%20Color/laravel-mark-rgb-red.svg" width="32" height="32" alt="Laravel"></td>
<td><a href="https://www.php.net/"><b>PHP</b></a> + <a href="https://laravel.com/"><b>Laravel</b></a></td>
<td><a href="./php-laravel/"><code>php-laravel/</code></a></td>
<td><a href="./php-laravel/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/python/python-original.svg" width="32" height="32" alt="Python"> <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/fastapi/fastapi-original.svg" width="32" height="32" alt="FastAPI"></td>
<td><a href="https://www.python.org/"><b>Python</b></a> + <a href="https://fastapi.tiangolo.com/"><b>FastAPI</b></a></td>
<td><a href="./python-fastapi/"><code>python-fastapi/</code></a></td>
<td><a href="./python-fastapi/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/typescript/typescript-original.svg" width="32" height="32" alt="TypeScript"> <img src="https://hono.dev/images/logo.png" width="36" height="36" alt="Hono"></td>
<td><a href="https://www.typescriptlang.org/"><b>TypeScript</b></a> + <a href="https://hono.dev/"><b>Hono</b></a></td>
<td><a href="./ts-hono/"><code>ts-hono/</code></a></td>
<td><a href="./ts-hono/README.md">README.md</a></td>
<td align="center">✅ 完了</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/rust/rust-original.svg" width="32" height="32" alt="Rust"> <img src="https://www.aldeka.net/_app/immutable/assets/ferris.5bb4776d.png" height="32" alt="Axum"></td>
<td><a href="https://www.rust-lang.org/"><b>Rust</b></a> + <a href="https://github.com/tokio-rs/axum"><b>Axum</b></a></td>
<td><a href="./rust-axum/"><code>rust-axum/</code></a></td>
<td><a href="./rust-axum/README.md">README.md</a></td>
<td align="center">🚧 予定</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/kotlin/kotlin-original.svg" width="32" height="32" alt="Kotlin"> <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/ktor/ktor-original.svg" width="32" height="32" alt="Ktor"></td>
<td><a href="https://kotlinlang.org/"><b>Kotlin</b></a> + <a href="https://ktor.io/"><b>Ktor</b></a></td>
<td><a href="./kotlin-ktor/"><code>kotlin-ktor/</code></a></td>
<td><a href="./kotlin-ktor/README.md">README.md</a></td>
<td align="center">🚧 予定</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/ruby/ruby-original.svg" width="32" height="32" alt="Ruby"> <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/rails/rails-plain.svg" width="32" height="32" alt="Rails"></td>
<td><a href="https://www.ruby-lang.org/"><b>Ruby</b></a> + <a href="https://rubyonrails.org/"><b>Ruby on Rails</b></a></td>
<td><a href="./ruby-rails/"><code>ruby-rails/</code></a></td>
<td><a href="./ruby-rails/README.md">README.md</a></td>
<td align="center">🚧 予定</td>
</tr>
<tr>
<td align="center"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/ruby/ruby-original.svg" width="32" height="32" alt="Ruby"> <img src="https://avatars.githubusercontent.com/u/3210273" width="32" height="32" alt="Hanami"></td>
<td><a href="https://www.ruby-lang.org/"><b>Ruby</b></a> + <a href="https://hanamirb.org/"><b>Hanami</b></a></td>
<td><a href="./ruby-hanami/"><code>ruby-hanami/</code></a></td>
<td><a href="./ruby-hanami/README.md">README.md</a></td>
<td align="center">🚧 予定</td>
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
