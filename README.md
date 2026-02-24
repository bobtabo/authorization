# authorization-swagger

![Swagger Logo](https://raw.githubusercontent.com/swagger-api/swagger.io/wordpress/images/assets/SW-logo-clr.png)

OpenAPI仕様を Swagger UI でローカル確認するためのリポジトリです。  
Docker Compose で起動します。

---

## 🛠 事前準備

`.env.example` をコピーして `.env` を作成してください。

```bash
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
http://localhost:8080
```

---

## 🛑 停止方法

```bash
docker compose down
```

---

## 📁 ディレクトリ構成

```
authorization-swagger/
├── docker-compose.yml
├── .env.example
├── .gitignore
└── docs/
    ├── index.html
    └── openapi.yml
```