AppConfig = Struct.new(
  :env, :port, :runtime, :frontend_url, :staff_cookie_lifetime, :notification_default_limit, :cache_prefix,
  keyword_init: true
)

DbConfig = Struct.new(:dsn, keyword_init: true)

RedisConfig = Struct.new(:host, :port, :password, :db, keyword_init: true)

OAuthConfig = Struct.new(
  :google_client_id, :google_client_secret, :google_redirect_url,
  :github_client_id, :github_client_secret, :github_redirect_url,
  keyword_init: true
)

JwtConfig = Struct.new(:issuer, :algorithm, :ttl, :cache_ttl, keyword_init: true)

MailConfig = Struct.new(
  :host, :port, :username, :password, :from_address, :app_name, :app_env,
  keyword_init: true
)

Config = Struct.new(:app, :db, :redis, :oauth, :jwt, :mail, keyword_init: true)

module ConfigLoader
  def self.load
    dsn = "mysql2://#{ENV.fetch('DB_USERNAME', 'root')}:#{ENV.fetch('DB_PASSWORD', '')}"\
          "@#{ENV.fetch('DB_HOST', 'localhost')}:#{ENV.fetch('DB_PORT', '3306')}"\
          "/#{ENV.fetch('DB_DATABASE', 'authorization')}"

    Config.new(
      app: AppConfig.new(
        env:                        ENV.fetch('APP_ENV', 'local'),
        port:                       ENV.fetch('APP_PORT', '8080'),
        runtime:                    ENV.fetch('APP_RUNTIME', 'rb-rails'),
        frontend_url:               ENV.fetch('FRONTEND_URL', 'http://localhost:3000'),
        staff_cookie_lifetime:      ENV.fetch('STAFF_COOKIE_LIFETIME', '60').to_i,
        notification_default_limit: ENV.fetch('NOTIFICATION_DEFAULT_LIMIT', '10').to_i,
        cache_prefix:               ENV.fetch('CACHE_PREFIX', ''),
      ),
      db: DbConfig.new(dsn: dsn),
      redis: RedisConfig.new(
        host:     ENV.fetch('REDIS_HOST', 'localhost'),
        port:     ENV.fetch('REDIS_PORT', '6379').to_i,
        password: ENV.fetch('REDIS_PASSWORD', ''),
        db:       ENV.fetch('REDIS_DB', '0').to_i,
      ),
      oauth: OAuthConfig.new(
        google_client_id:     ENV.fetch('GOOGLE_CLIENT_ID', ''),
        google_client_secret: ENV.fetch('GOOGLE_CLIENT_SECRET', ''),
        google_redirect_url:  ENV.fetch('GOOGLE_REDIRECT_URL', ''),
        github_client_id:     ENV.fetch('GITHUB_CLIENT_ID', ''),
        github_client_secret: ENV.fetch('GITHUB_CLIENT_SECRET', ''),
        github_redirect_url:  ENV.fetch('GITHUB_REDIRECT_URL', ''),
      ),
      jwt: JwtConfig.new(
        issuer:    'authorization',
        algorithm: 'RS256',
        ttl:       1800,
        cache_ttl: ENV.fetch('GATE_JWT_CACHE_TTL', '1800').to_i,
      ),
      mail: MailConfig.new(
        host:         ENV.fetch('MAIL_HOST', 'localhost'),
        port:         ENV.fetch('MAIL_PORT', '1025'),
        username:     ENV.fetch('MAIL_USERNAME', ''),
        password:     ENV.fetch('MAIL_PASSWORD', ''),
        from_address: ENV.fetch('MAIL_FROM_ADDRESS', 'no-reply@example.com'),
        app_name:     ENV.fetch('APP_NAME', 'Authorization Gateway'),
        app_env:      ENV.fetch('APP_ENV', 'local'),
      ),
    )
  end
end
