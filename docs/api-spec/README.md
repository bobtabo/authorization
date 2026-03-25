# authorization-docs

![Swagger Logo](https://raw.githubusercontent.com/swagger-api/swagger.io/wordpress/images/assets/SW-logo-clr.png)

OpenAPI仕様を Swagger UI でローカル確認するためのリポジトリです。  
Docker Compose で起動します。

API 仕様のドキュメントルートは **`docs/api-spec/`** です。

---

## 🛠 事前準備

`.env.example` をコピーして `.env` を作成してください。

```bash
cd docs/api-spec
cp .env.example .env
```

---

## 🚀 起動方法

プロジェクトルートで実行：

```bash
docker compose up -d
```

ブラウザでアクセス：

```
http://localhost:8082
```

---

## 🛑 停止方法

```bash
cd docs
docker compose down
```

---

## 📁 ディレクトリ構成

```
authorization/docs/api-spec/    # API ドキュメントルート
├── .env.example
├── .gitignore
├── docker-compose.yml
├── index.html
├── openapi.yml
├── README.md
└── swagger-ui/
    ├── index.css
    ├── swagger-ui.css
    ├── swagger-ui.js
    ・・・
```
