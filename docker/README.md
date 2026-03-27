## ディレクトリ構成

| パス                                                           | 内容                                                                 |
|--------------------------------------------------------------|--------------------------------------------------------------------|
| [`develop/`](./develop/)                                     | AWSの開発環境用を想定                                                       |
| [`local/backends/app-go/`](./local/backends/app-go/)         | Go 実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける想定。                     |
| [`local/backends/app-php/`](./local/backends/app-php/)       | PHP 実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける想定。                    |
| [`local/backends/app-python/`](./local/backends/app-python/) | Python 実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける想定。                 |
| [`local/backends/app-ts/`](./local/backends/app-ts/)         | TypeScript 実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける想定。             |
| [`local/backends/common/`](./local/backends/common/)         | 共通インフラ（リバースプロキシ、DB、キャッシュ、メール検証用など）。複数バックエンドで共有する前提のスタック。 |
| [`local/frontend`](./local/frontend/)                        | ローカルのフロントエンド用（今のところ必要なし）                                           |
| [`production/`](./production/)                               | AWSの本番環境用を想定                                                       |
| [`staging/`](./staging/)                                     | AWSの検証環境用を想定                                                       |

`common` 側で Docker ネットワーク `authorization` を作成し、各 `docker-compose` はそのネットワークに参加します（`external: true`）。

## 前提

- Docker Engine および Docker Compose（`docker compose` または `docker-compose`）が使えること
- ポート **443**（プロキシ）、**3306**（MySQL）、**6379**（Redis）などがローカルで空いていること（`.env` で変更可）

## コンテナ起動（共通インフラ）

共通スタックの詳細・ツール URL は [`local/backends/common/README.md`](./local/backends/common/README.md) を参照してください。

```bash
cd docker

# 初回のみ: スクリプトに実行権限、証明書・環境変数
find ./bin -type f -exec chmod 755 {} +
bin/docker-environment.sh
# .env を編集してよい

# 起動（内部で authorization ネットワーク作成 + compose up）
bin/docker-common-up.sh
```

停止・削除（**ボリュームや data も消える**ので注意）:

```bash
bin/docker-common-down.sh
```

MailHog の UI 例: `http://localhost:8025/`（ポートは `.env` の `MAILHOG_PORT` 依存）

## PHP アプリ用スタック（任意）

`common` でネットワークとプロキシが立ち上がった状態で、PHP 側の compose を使います。

```bash
cd local/backends/app-php
cp .env.example .env
# SERVER_NAME や APP_NAME、マウント先パスに合わせて編集

docker compose up -d --build
```

アプリソースのマウント先は `docker-compose.yml` 内の `./../../web` です。リポジトリ構成に合わせてパスを調整するか、シンボリックリンクで合わせてください。

## 注意

- `docker-down.sh` は **データディレクトリやログを削除する**処理が入っています。実行前に内容を確認してください。
- 証明書・パスワード類は **開発用サンプル**です。共有環境では流用しないでください。

## 関連ドキュメント

- リポジトリ全体: [../README.md](../README.md)
