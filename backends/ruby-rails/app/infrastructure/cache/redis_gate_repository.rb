# frozen_string_literal: true
#
# Redis を用いたゲートキャッシュリポジトリの実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Cache
    # Redis を用いたゲートキャッシュリポジトリの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class RedisGateRepository
      # @param cfg [AppConfig] アプリケーション設定
      def initialize(cfg)
        @redis  = RedisClient.new_client(cfg)
        @prefix = cfg.app.cache_prefix
      end

      def get_jwt(identifier, member_id)
        @redis.get(cache_key(identifier, member_id))
      end

      def put_jwt(identifier, member_id, token, ttl)
        @redis.set(cache_key(identifier, member_id), token, ex: ttl)
        nil
      end

      private

      def cache_key(identifier, member_id)
        "#{@prefix}:gate.jwt:#{identifier}:#{member_id}"
      end
    end
  end
end
