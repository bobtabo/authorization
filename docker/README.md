<p align="center">
    <span style="margin-right:50px;"><img src="https://cdn.worldvectorlogo.com/logos/nginx.svg" width=120px /></span>
    <span style="margin-right:50px;"><img src="https://cdn.cdnlogo.com/logos/m/10/mysql.svg" width=100px /></span>
    <span><img src="https://www.vectorlogo.zone/logos/redis/redis-ar21.svg" width=100px /></span>
</p>

<p align="center">
    <span style="margin-right:10px;"><img src="https://img.shields.io/badge/nginx-v1.x-008000.svg?style=flat"></span>
    <span style="margin-right:10px;"><img src="https://img.shields.io/badge/mysql-v8.x-1e90ff.svg?style=flat"></span>
    <span><img src="https://img.shields.io/badge/redis-stable-ff7964.svg?style=flat"></span>
</p>

---

## :file_folder: ディレクトリ構成

| パス                                                           | 内容                                                                 |
|--------------------------------------------------------------|--------------------------------------------------------------------|
| [`develop/`](./develop/)                                     | AWSの開発環境用を想定                                                       |
| [`local/app-go/`](local/app-go/)         | Go 実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける想定。                     |
| [`local/app-php/`](local/app-php/)       | PHP 実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける想定。                    |
| [`local/app-python/`](local/app-python/) | Python 実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける想定。                 |
| [`local/app-ts/`](local/app-ts/)         | TypeScript 実行環境。`jwilder/nginx-proxy` 経由でホスト名で振り分ける想定。             |
| [`local/common/`](local/common/)         | 複数バックエンドで共有する共通インフラ。 |
| [`production/`](./production/)                               | AWSの本番環境用を想定                                                       |
| [`staging/`](./staging/)                                     | AWSの検証環境用を想定                                                       |

`common` 側で Docker ネットワーク `authorization` を作成し、各 `docker-compose` はそのネットワークに参加します（`external: true`）。

## :white_check_mark: 前提

- Docker Engine および Docker Compose（`docker compose` または `docker-compose`）が使えること
- ポート **443**（プロキシ）、**3306**（MySQL）、**6379**（Redis）などがローカルで空いていること（`.env` で変更可）

## :whale: 共通コンテナ操作

### 事前準備
```bash
cd docker

# 初回のみ: スクリプトに実行権限、証明書・環境変数
find ./bin -type f -exec chmod 755 {} +
bin/docker-environment.sh
```

### コンテナを起動する
```bash
# 起動（内部で authorization ネットワーク作成 + compose up）
bin/docker-common.sh up
```

### コンテナを停止する
```bash
bin/docker-common.sh stop
```

### コンテナを再開する
```bash
bin/docker-common.sh start
```

### コンテナを破棄する
```bash
# ボリュームや data も消えるので注意！
bin/docker-common.sh down
```

## :gear: アプリコンテナ操作

`common` でネットワークとプロキシが立ち上がった状態で、各アプリ環境を起動します。

### コンテナを起動する

```bash
# Go環境を起動する
bin/docker-go.sh up

# PHP環境を起動する
bin/docker-php.sh up

# Python環境を起動する
bin/docker-python.sh up

# TypeScript環境を起動する
bin/docker-ts.sh up
```

### コンテナに入る

```bash
# Go環境に入る
bin/docker-go.sh exec

# PHP環境に入る
bin/docker-php.sh exec

# Python環境に入る
bin/docker-python.sh exec

# TypeScript環境に入る
bin/docker-ts.sh exec
```

### コンテナを破棄する

```bash
# Go環境を破棄する
bin/docker-go.sh down

# PHP環境を破棄する
bin/docker-php.sh down

# Python環境を破棄する
bin/docker-python.sh down

# TypeScript環境を破棄する
bin/docker-ts.sh down
```

### 全コンテナを起動する

```bash
bin/docker-backends.sh up
```

### コンテナを破棄する

```bash
bin/docker-backends.sh down
```

## :fire: 注意

- `docker-xxx-down.sh` は **データディレクトリやログを削除する**処理が入っています。実行前に内容を確認してください。
- 証明書・パスワード類は **開発用サンプル**です。共有環境では流用しないでください。

# :bulb: 各ツール

| ツール     | URL |
|---------| ---- |
| MailPit | http://localhost:8025/ |
