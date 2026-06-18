<p align="center">
<a href="https://www.terraform.io/" target="_blank"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/terraform/terraform-original-wordmark.svg" height="72" alt="Terraform"></a>
&nbsp;&nbsp;
<a href="https://localstack.cloud/" target="_blank"><img src="https://avatars.githubusercontent.com/u/28732122?s=200&v=4" height="72" alt="LocalStack"></a>
</p>

<p align="center">
<a href="https://www.terraform.io/"><img src="https://img.shields.io/badge/Terraform-1.x-7B42BC?logo=terraform&logoColor=white" alt="Terraform"></a>
<a href="https://github.com/localstack/terraform-local"><img src="https://img.shields.io/badge/tflocal-latest-4728E3?logoColor=white" alt="tflocal"></a>
</p>

---

# Terraform IaC 定義

このディレクトリは、認可サーバーのインフラストラクチャを **Terraform** で IaC 管理するための定義ファイルを環境別に格納しています。

---

## :file_folder: ディレクトリ構成

```
terraform/
├── local/          # LocalStack 向け（ローカル開発環境）
├── develop/        # 実 AWS 開発環境用（予約）
├── staging/        # 実 AWS ステージング環境用（予約）
└── production/     # 実 AWS 本番環境用（予約）
```

---

## :gear: 環境別ディレクトリ

| ディレクトリ | 用途 | 状態 |
|:---|:---|:---|
| [`local/`](./local/) | LocalStack でのローカル開発・検証 | **利用中** |
| `develop/` | 実 AWS 開発環境へのデプロイ | 予約（未実装） |
| `staging/` | 実 AWS ステージング環境へのデプロイ | 予約（未実装） |
| `production/` | 実 AWS 本番環境へのデプロイ | 予約（未実装） |

---

## :rocket: ローカル開発（LocalStack）

ローカル開発環境の詳細は [`local/README.md`](./local/README.md) を参照してください。

```bash
# クイックスタート
cd terraform/local
make apply    # LocalStack にリソースを作成
make output   # API Gateway URL 等を確認
make destroy  # リソースを削除
```

### 管理リソース

- **API Gateway** — REST API（`/{proxy+}` → Lambda）
- **Lambda** — Go バイナリ（`function/function.zip`）
- **SES** — ドメイン認証・送信元アドレス
- **SSM Parameter Store** — DB / Redis / OAuth / アプリ設定

---

## :white_check_mark: 前提

- [Terraform](https://developer.hashicorp.com/terraform/install) がインストール済みであること
- [tflocal](https://github.com/localstack/terraform-local)（`pip install terraform-local`）がインストール済みであること
- ローカル環境では Docker Compose で LocalStack コンテナが起動済みであること
