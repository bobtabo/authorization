# frozen_string_literal: true

require "dotenv"
# ローカルコンテナ用（host.docker.internal）を優先、なければ CI 用（127.0.0.1）
# Dotenv.load は既存の env var を上書きしないため CI の env vars が最優先される
Dotenv.load(
  File.expand_path("../.env.testing.local", __dir__),
  File.expand_path("../.env.testing",       __dir__),
)

require "hanami/action"
require "hanami/router"
require "rack/test"
require "rspec"
require "json"
require "openssl"
require "securerandom"
require "redis"

# Authorization::Action は Hanami::Action のエイリアス
# （app/action.rb はサーバー起動時に Hanami::App がロードするため、テストでは直接定義する）
module Authorization
  Action = Hanami::Action unless const_defined?(:Action)
end

# アプリコードを直接 require（サーバー起動不要）
APP_ROOT = File.expand_path("..", __dir__)
[
  "app/config/app_config.rb",
  "app/support/**/*.rb",
  "app/domain/**/*.rb",
  "app/usecase/**/*.rb",
  "app/infrastructure/**/*.rb",
  "app/middleware/**/*.rb",
  "app/requests/**/*.rb",
  "app/actions/base.rb",
  "app/actions/**/*.rb",
  "app/config/container.rb",
].each { |pattern| Dir[File.join(APP_ROOT, pattern)].sort.each { |f| require f } }

# テスト用 Rack アプリ（Hanami::Router を直接使用、実 AppContainer を通じて実 DB へ接続）
TEST_APP = Hanami::Router.new do
  get  "/auth/google/redirect",                   to: Authorization::Actions::Auth::GoogleRedirect.new
  get  "/auth/google/callback",                   to: Authorization::Actions::Auth::GoogleCallback.new
  get  "/api/auth/me",                            to: Authorization::Actions::Auth::Me.new
  get  "/api/auth/login",                         to: Authorization::Actions::Auth::Login.new
  get  "/api/auth/logout",                        to: Authorization::Actions::Auth::Logout.new
  get  "/api/auth/invitation/:token",             to: Authorization::Actions::Auth::Invitation.new
  get    "/api/clients",                            to: Authorization::Actions::Clients::Index.new
  post   "/api/clients/store",                      to: Authorization::Actions::Clients::Store.new
  get    "/api/clients/:id",                        to: Authorization::Actions::Clients::Show.new
  put    "/api/clients/:id/update",                 to: Authorization::Actions::Clients::Update.new
  delete "/api/clients/:id/delete",                 to: Authorization::Actions::Clients::Destroy.new
  get    "/api/clients/:identifier/qr",             to: Authorization::Actions::Clients::Qr.new
  get    "/api/clients/:identifier/info",           to: Authorization::Actions::Clients::Info.new
  patch  "/api/clients/:identifier/start",          to: Authorization::Actions::Clients::Start.new
  patch  "/api/clients/:identifier/stop",           to: Authorization::Actions::Clients::Stop.new
  get    "/api/staffs",                           to: Authorization::Actions::Staffs::Index.new
  patch  "/api/staffs/:id/updateRole",            to: Authorization::Actions::Staffs::UpdateRole.new
  patch  "/api/staffs/:id/restore",               to: Authorization::Actions::Staffs::Restore.new
  delete "/api/staffs/:id/delete",                to: Authorization::Actions::Staffs::Destroy.new
  get    "/api/admin/invitation",                 to: Authorization::Actions::Admin::Invitations::Index.new
  get    "/api/admin/invitation/issue",           to: Authorization::Actions::Admin::Invitations::Issue.new
  get    "/api/gate/issue",                       to: Authorization::Actions::Gate::Issue.new
  get    "/api/gate/client/:identifier/verify",   to: Authorization::Actions::Gate::Verify.new
  get    "/api/notifications/counts",             to: Authorization::Actions::Notifications::Counts.new
  get    "/api/notifications",                    to: Authorization::Actions::Notifications::Index.new
  patch  "/api/notifications",                    to: Authorization::Actions::Notifications::ReadAll.new
  patch  "/api/notifications/:id",               to: Authorization::Actions::Notifications::Read.new
end

RSpec.configure do |config|
  config.include Rack::Test::Methods
  config.expect_with :rspec do |c|
    c.syntax = :expect
  end
end

def app
  Rack::Builder.new do
    use Middleware::JsonBodyParser
    run TEST_APP
  end
end

# ---------- DB / Redis ヘルパー ----------

def db
  AppContainer.instance[:rom].gateways[:default].connection
end

def truncate_tables
  db.run("SET FOREIGN_KEY_CHECKS=0")
  %w[notifications invitations clients staffs].each { |t| db.run("TRUNCATE TABLE #{t}") }
  db.run("SET FOREIGN_KEY_CHECKS=1")
  cfg = AppContainer.instance[:cfg]
  r = Redis.new(host: cfg.redis.host, port: cfg.redis.port)
  r.flushdb
  r.close
rescue StandardError => e
  warn "truncate_tables warning: #{e.message}"
end

# ---------- テストデータ生成ヘルパー ----------

def create_staff(overrides = {})
  now = Time.now
  attrs = {
    name:        "テストスタッフ",
    email:       "staff-#{SecureRandom.hex(4)}@example.com",
    provider:    1,
    provider_id: "test-#{SecureRandom.hex(8)}",
    role:        1,
    created_at:  now,
    updated_at:  now,
    created_by:  0,
    updated_by:  0,
    version:     1,
  }.merge(overrides)
  id = db[:staffs].insert(attrs)
  db[:staffs].where(id: id).first
end

def create_client(overrides = {})
  key   = OpenSSL::PKey::RSA.generate(2048)
  token = SecureRandom.hex(32)
  now   = Time.now
  attrs = {
    name:         "テストクライアント",
    identifier:   "test-client-#{SecureRandom.hex(4)}",
    post_code:    "100-0001",
    pref:         "東京都",
    city:         "千代田区",
    address:      "千代田1-1",
    building:     "",
    tel:          "0312345678",
    email:        "client-#{SecureRandom.hex(4)}@example.com",
    access_token: token,
    private_key:  key.to_pem,
    public_key:   key.public_key.to_pem,
    fingerprint:  "SHA256:test",
    status:       1,
    created_at:   now,
    updated_at:   now,
    created_by:   0,
    updated_by:   0,
    version:      1,
  }.merge(overrides)
  id = db[:clients].insert(attrs)
  db[:clients].where(id: id).first
end

def create_invitation(token = SecureRandom.hex(16), role: 2)
  now = Time.now
  id = db[:invitations].insert(
    token:      token,
    role:       role,
    created_at: now,
    updated_at: now,
    created_by: 0,
    updated_by: 0,
    version:    1,
  )
  db[:invitations].where(id: id).first
end

def create_notification(staff_id, title, overrides = {})
  now = Time.now
  attrs = {
    staff_id:     staff_id,
    message_type: 1,
    title:        title,
    message:      "テスト通知本文",
    read:         false,
    created_at:   now,
    updated_at:   now,
    created_by:   0,
    updated_by:   0,
    version:      1,
  }.merge(overrides)
  id = db[:notifications].insert(attrs)
  db[:notifications].where(id: id).first
end
