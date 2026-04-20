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

# Authorization::Action は Hanami::Action のエイリアス
# （通常は Hanami::App の起動時に生成されるが、テストではここで直接定義する）
module Authorization
  Action = Hanami::Action unless const_defined?(:Action)
end

# アプリコードを直接 require（サーバー起動不要）
APP_ROOT = File.expand_path("..", __dir__)
[
  "app/config/app_config.rb",
  "app/domain/**/*.rb",
  "app/usecase/**/*.rb",
  "app/infrastructure/**/*.rb",
  "app/actions/base.rb",
  "app/actions/**/*.rb",
  "app/config/container.rb",
].each { |pattern| Dir[File.join(APP_ROOT, pattern)].sort.each { |f| require f } }

# テスト用 Rack アプリ（Hanami::Router を直接使用）
TEST_APP = Hanami::Router.new do
  get  "/auth/google/redirect",                   to: Authorization::Actions::Auth::GoogleRedirect.new
  get  "/auth/google/callback",                   to: Authorization::Actions::Auth::GoogleCallback.new
  get  "/api/auth/me",                            to: Authorization::Actions::Auth::Me.new
  get  "/api/auth/login",                         to: Authorization::Actions::Auth::Login.new
  get  "/api/auth/logout",                        to: Authorization::Actions::Auth::Logout.new
  get  "/api/auth/invitation/:token",             to: Authorization::Actions::Auth::Invitation.new
  get  "/api/clients",                            to: Authorization::Actions::Clients::Index.new
  post "/api/clients/store",                      to: Authorization::Actions::Clients::Store.new
  get  "/api/clients/:id",                        to: Authorization::Actions::Clients::Show.new
  put  "/api/clients/:id/update",                 to: Authorization::Actions::Clients::Update.new
  delete "/api/clients/:id/delete",               to: Authorization::Actions::Clients::Destroy.new
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
  TEST_APP
end

# ---------- AppContainer スタブ ----------

def stub_container(overrides = {})
  defaults = {
    cfg:             double("cfg", app: double("app_cfg", notification_default_limit: 10)),
    auth_uc:         double("auth_uc"),
    client_uc:       double("client_uc"),
    staff_uc:        double("staff_uc"),
    invitation_uc:   double("invitation_uc"),
    gate_uc:         double("gate_uc"),
    notification_uc: double("notification_uc"),
    mailer:          double("mailer", send_access_token: nil),
  }
  container = defaults.merge(overrides)
  allow(AppContainer).to receive(:instance).and_return(container)
  container
end

# ---------- フィクスチャ ----------

def staff_fixture(overrides = {})
  double("staff",
    { id: 1, name: "テストスタッフ", email: "staff@example.com", avatar: nil, role: 1,
      created_at: Time.now, updated_at: Time.now, deleted_at: nil }.merge(overrides)
  )
end

def client_fixture(overrides = {})
  double("client",
    { id: 1, name: "テストクライアント", identifier: "test-client-001",
      post_code: "100-0001", pref: "東京都", city: "千代田区",
      address: "千代田1-1", building: nil, tel: "0312345678",
      email: "client@example.com", access_token: "token-abc",
      status: 1, start_at: nil, stop_at: nil,
      created_at: Time.now, updated_at: Time.now }.merge(overrides)
  )
end

def invitation_fixture(overrides = {})
  double("invitation",
    { url: "http://localhost:3000/invite/abc", display_url: "localhost:3000/invite/abc",
      token: "abc123" }.merge(overrides)
  )
end

def notification_fixture(overrides = {})
  double("notification",
    { id: 1, staff_id: 1, message_type: 1, title: "テスト通知", message: "本文",
      url: "/clients/show?id=1", read: false,
      created_at: Time.now, updated_at: Time.now }.merge(overrides)
  )
end

def notification_page_fixture(items)
  double("page", items: items, next_cursor: nil)
end
