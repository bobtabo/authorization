# LocalStack + Terraform 操作用 Makefile
# 事前に pip install terraform-local でtflocal コマンドをインストールしておくこと

.PHONY: init plan apply destroy output clean setup-env

# Terraform 初期化
init:
	tflocal init

# 差分確認
plan: init
	tflocal plan

# リソース作成・更新（完了後に .env.local を自動生成）
apply: init
	tflocal apply -auto-approve
	@$(MAKE) setup-env

# リソース削除
destroy:
	tflocal destroy -auto-approve

# 出力値の表示（API Gateway URL 等）
output:
	tflocal output

# frontend/.env.local を生成（API Gateway ID を自動解決）
setup-env:
	@bash scripts/setup-env.sh

# Terraform キャッシュの削除
clean:
	rm -rf .terraform .terraform.lock.hcl terraform.tfstate terraform.tfstate.backup
