require "hanami"
require "bundler/setup"
Bundler.require(:default)
require "dotenv"

# .env.local → .env の順で読み込む（既存の環境変数は上書きしない）
Dotenv.load(
  File.expand_path("../.env.local", __dir__),
  File.expand_path("../.env",       __dir__),
)

# app/ 配下の top-level 定数ファイルは Zeitwerk の命名規則（Authorization::*）に従っていないため
# Hanami の autoloader を設定する前に明示的に require する
APP_ROOT_LOAD = File.expand_path("..", __dir__)
[
  "app/config/app_config.rb",
  "app/support/**/*.rb",
  "app/domain/**/*.rb",
  "app/usecase/**/*.rb",
  "app/infrastructure/**/*.rb",
  "app/middleware/**/*.rb",
].each { |pattern| Dir[File.join(APP_ROOT_LOAD, pattern)].sort.each { |f| require f } }

require_relative "../app/config/container"

module Authorization
  class App < Hanami::App
    config.middleware.use Middleware::JsonBodyParser
  end
end
