ENV["BUNDLE_GEMFILE"] ||= File.expand_path("../Gemfile", __dir__)
require "bundler/setup"

# テスト時に dotenv-rails より先に env ファイルを先読みして優先させる。
# .env.testing.local（ローカルコンテナ用 host.docker.internal）を先に読み、
# なければ .env.testing（CI 用 127.0.0.1）を読む。
# Dotenv.load は既存の値を上書きしないため、後から dotenv-rails が .env を
# 読んでも先に設定された値が保持される。
if ENV["RAILS_ENV"] == "test"
  require "dotenv"
  Dotenv.load(
    File.expand_path("../.env.testing.local", __dir__),
    File.expand_path("../.env.testing",       __dir__),
  )
end
