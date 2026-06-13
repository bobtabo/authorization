# LocalStack + Terraform 操作用 Makefile
# 事前に pip install terraform-local でtflocal コマンドをインストールしておくこと

.PHONY: init plan apply destroy output clean

# Terraform 初期化
init:
	tflocal init

# 差分確認
plan: init
	tflocal plan

# リソース作成・更新
apply: init
	tflocal apply -auto-approve

# リソース削除
destroy:
	tflocal destroy -auto-approve

# 出力値の表示（API Gateway URL 等）
output:
	tflocal output

# Terraform キャッシュの削除
clean:
	rm -rf .terraform .terraform.lock.hcl terraform.tfstate terraform.tfstate.backup
