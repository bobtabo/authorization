<p align="center">
<a href="https://swagger.io/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/swagger/swagger-original.svg" height="72" alt="Swagger"></a>
&nbsp;&nbsp;
<a href="https://www.openapis.org/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/openapi/openapi-original.svg" height="72" alt="OpenAPI"></a>
</p>

<p align="center">
<a href="https://swagger.io/tools/swagger-ui/"><img src="https://img.shields.io/badge/Swagger_UI-5.30.2-85EA2D?logo=swagger&logoColor=black" alt="Swagger UI 5.30.2"></a>
<img src="https://img.shields.io/badge/OpenAPI-3.0.3-6BA539?logo=openapiinitiative&logoColor=white" alt="OpenAPI 3.0.3">
</p>

---

OpenAPI 仕様を **Swagger UI** でローカル確認するための環境です。  
Docker Compose で起動します。

---

## :file_folder: ディレクトリ構成

```
docs/api-spec/
├── openapi.yml         # API 仕様（OpenAPI 3.0）
├── docker-compose.yml  # Swagger UI 起動設定
├── .env.example
├── index.html
└── swagger-ui/         # Swagger UI 静的ファイル
```

---

## :rocket: セットアップ・起動

### 1. 環境変数の設定

```bash
cd docs/api-spec
cp .env.example .env
```

### 2. 起動

```bash
docker compose up -d
```

ブラウザでアクセス：`http://localhost:8082`

---

## :stop_sign: 停止

```bash
docker compose down
```
