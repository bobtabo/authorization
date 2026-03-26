# AWS Lambda custom runtime (provided.al2023) 向けに bootstrap バイナリを生成する。
# lambda.norpc: aws-lambda-go の RPC モードを使わず JSON 直列化で呼び出す（推奨タグ）。
BINARY := bootstrap
ZIP := function.zip

.PHONY: build build-HttpFunction build-arm64 zip zip-arm64 clean test

# SAM（BuildMethod: makefile）が呼ぶ想定のターゲット（template.yaml の HttpFunction に対応）
build-HttpFunction: build

# ユニットテスト（-race でデータ競合も検査）
test:
	go test -race -count=1 ./...

# Linux x86_64（Lambda デフォルトアーキテクチャ向け）。エントリは ./cmd 。
build:
	GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -tags lambda.norpc -ldflags="-s -w" -o $(BINARY) ./cmd

# Linux arm64（Graviton 向け）
build-arm64:
	GOOS=linux GOARCH=arm64 CGO_ENABLED=0 go build -tags lambda.norpc -ldflags="-s -w" -o $(BINARY) ./cmd

# デプロイ用 ZIP（ルートに bootstrap のみ）
zip: build
	rm -f $(ZIP)
	zip $(ZIP) $(BINARY)

zip-arm64: build-arm64
	rm -f $(ZIP)
	zip $(ZIP) $(BINARY)

clean:
	rm -f $(BINARY) $(ZIP)
