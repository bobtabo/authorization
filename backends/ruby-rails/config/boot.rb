ENV["BUNDLE_GEMFILE"] ||= File.expand_path("../Gemfile", __dir__)
require "bundler/setup"

# ENV_FILE が指定されている場合は dotenv-rails より先にロードして優先させる
# ローカルコンテナ: ENV_FILE=.env.testing.local bundle exec rspec
# CI: ENV_FILE 未設定 → dotenv-rails が .env をロード
if (env_file = ENV["ENV_FILE"])
  require "dotenv"
  Dotenv.load(env_file)
end
