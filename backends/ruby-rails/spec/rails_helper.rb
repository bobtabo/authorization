# This file is copied to spec/ when you run 'rails generate rspec:install'
require 'spec_helper'
ENV['RAILS_ENV'] ||= 'test'
require_relative '../config/environment'
# Prevent database truncation if the environment is production
abort("The Rails environment is running in production mode!") if Rails.env.production?
# Uncomment the line below in case you have `--require rails_helper` in the `.rspec` file
# that will avoid rails generators crashing because migrations haven't been run yet
# return unless Rails.env.test?
require 'rspec/rails'
# Add additional requires below this line. Rails is not loaded until this point!

# Zeitwerk registers app/domain, app/usecase, app/infrastructure as namespace-less roots,
# but the files define constants with Domain::, UseCase::, Infrastructure:: prefixes.
# Ignore those directories in Zeitwerk, then load them with Ruby's `load` directly so
# the constants (Domain::Client::Entity etc.) are defined before specs run.
%w[config domain usecase infrastructure].each do |dir|
  path = Rails.root.join("app/#{dir}")
  Rails.autoloaders.each { |loader| loader.ignore(path) }
  Dir["#{path}/**/*.rb"].sort.each { |f| load f }
end

# Requires supporting ruby files with custom matchers and macros, etc, in
# spec/support/ and its subdirectories. Files matching `spec/**/*_spec.rb` are
# run as spec files by default. This means that files in spec/support that end
# in _spec.rb will both be required and run as specs, causing the specs to be
# run twice. It is recommended that you do not name files matching this glob to
# end with _spec.rb. You can configure this pattern with the --pattern
# option on the command line or in ~/.rspec, .rspec or `.rspec-local`.
#
# The following line is provided for convenience purposes. It has the downside
# of increasing the boot-up time by auto-requiring all files in the support
# directory. Alternatively, in the individual `*_spec.rb` files, manually
# require only the support files necessary.
#
# Rails.root.glob('spec/support/**/*.rb').sort_by(&:to_s).each { |f| require f }

# Ensures that the test database schema matches the current schema file.
# If there are pending migrations it will invoke `db:test:prepare` to
# recreate the test database by loading the schema.
# If you are not using ActiveRecord, you can remove these lines.
begin
  ActiveRecord::Migration.maintain_test_schema!
rescue ActiveRecord::PendingMigrationError => e
  abort e.to_s.strip
end
require "openssl"
require "securerandom"

# 2048-bit RSA 鍵生成は重いため、モジュール読み込み時に 1 回だけ生成して再利用する。
CACHED_RSA_KEY = OpenSSL::PKey::RSA.generate(2048)

RSpec.configure do |config|
  config.include(Module.new do
    def create_staff(overrides = {})
      now = Time.current
      Infrastructure::Model::Staff.create!({
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
      }.merge(overrides))
    end

    def create_client(overrides = {})
      token = SecureRandom.hex(32)
      now   = Time.current
      Infrastructure::Model::Client.create!({
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
        private_key:  CACHED_RSA_KEY.to_pem,
        public_key:   CACHED_RSA_KEY.public_key.to_pem,
        fingerprint:  "SHA256:test",
        status:       1,
        created_at:   now,
        updated_at:   now,
        created_by:   0,
        updated_by:   0,
        version:      1,
      }.merge(overrides))
    end

    def create_invitation(token = SecureRandom.hex(16), role = 2)
      now = Time.current
      Infrastructure::Model::Invitation.create!(
        token:      token,
        role:       role,
        created_at: now,
        updated_at: now,
      )
    end

    def create_notification(staff_id, title, overrides = {})
      now = Time.current
      Infrastructure::Model::Notification.create!({
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
      }.merge(overrides))
    end
  end)

  # Remove this line if you're not using ActiveRecord or ActiveRecord fixtures
  config.fixture_paths = [
    Rails.root.join('spec/fixtures')
  ]

  # If you're not using ActiveRecord, or you'd prefer not to run each of your
  # examples within a transaction, remove the following line or assign false
  # instead of true.
  config.use_transactional_fixtures = true

  # テストスイート開始前にテーブルを truncate する。
  # ローカル環境で Ctrl+C などにより前回ロールバックされなかったデータを除去し、
  # 固定メールアドレスを使うテストの一意制約違反を防ぐ。
  config.before(:suite) do
    conn = ActiveRecord::Base.connection
    conn.execute("SET FOREIGN_KEY_CHECKS=0")
    %w[notifications invitations clients staffs].each { |t| conn.execute("TRUNCATE TABLE #{t}") }
    conn.execute("SET FOREIGN_KEY_CHECKS=1")
  end

  # You can uncomment this line to turn off ActiveRecord support entirely.
  # config.use_active_record = false

  # RSpec Rails uses metadata to mix in different behaviours to your tests,
  # for example enabling you to call `get` and `post` in request specs. e.g.:
  #
  #     RSpec.describe UsersController, type: :request do
  #       # ...
  #     end
  #
  # The different available types are documented in the features, such as in
  # https://rspec.info/features/8-0/rspec-rails
  #
  # You can also infer these behaviours automatically by location, e.g.
  # /spec/models would pull in the same behaviour as `type: :model` but this
  # behaviour is considered legacy and will be removed in a future version.
  #
  # To enable this behaviour uncomment the line below.
  # config.infer_spec_type_from_file_location!

  # Filter lines from Rails gems in backtraces.
  config.filter_rails_from_backtrace!
  # arbitrary gems may also be filtered via:
  # config.filter_gems_from_backtrace("gem name")
end
