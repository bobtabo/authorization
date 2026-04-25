# frozen_string_literal: true
#
# Redis クライアント生成モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Cache
    # Redis クライアントを生成するクラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class RedisClient
      # @param cfg [AppConfig] アプリケーション設定
      # @return [Redis] Redis クライアントインスタンス
      def self.new_client(cfg = ConfigLoader.load)
        Redis.new(
          host:     cfg.redis.host,
          port:     cfg.redis.port,
          password: cfg.redis.password.empty? ? nil : cfg.redis.password,
          db:       cfg.redis.db,
        )
      end
    end
  end
end
