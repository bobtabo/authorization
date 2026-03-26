# AWS Lambda（Go）

認可サーバー関連で利用する **AWS Lambda** 関数のコードです。ランタイムは **`provided.al2023`**（`bootstrap` バイナリ）を想定しています。

## 構成

```
function/
├── cmd/main.go           # エントリ（lambda.Start・依存の組み立て）
├── internal/
│   ├── handler/          # API Gateway HTTP API v2 向けハンドラ
│   ├── domain/           # ドメイン型・ビジネスロジック
│   └── adapter/          # DynamoDB / 外部 API 等の境界（実装は随時追加）
├── go.mod
├── Makefile
└── template.yaml         # AWS SAM
```

## 前提

- Go 1.22 以降
- デプロイに AWS SAM CLI を使う場合は [AWS SAM のインストール](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html) を参照

## 開発

```bash
cd function
go test -race -count=1 ./...
# または
make test
```

## ビルド（Lambda 用 ZIP）

Linux 向けに `bootstrap` を生成し、`function.zip` にまとめます。

```bash
make zip        # amd64
# make zip-arm64  # arm64（Graviton）
```

生成物: ルート直下の `bootstrap` と `function.zip`（`.gitignore` 対象）。

## SAM でデプロイ（例）

```bash
sam build
sam deploy --guided
```

`template.yaml` の関数リソースは **`Metadata.BuildMethod: makefile`** とし、`make build-HttpFunction` で `bootstrap` をビルドします。

## 関連ドキュメント

- リポジトリ全体: [../README.md](../README.md)
