# frozen_string_literal: true
#
# Redis を用いた招待認証キャッシュリポジトリの実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Cache
    # Redis を用いた招待認証キャッシュリポジトリの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class RedisInvitationAuthRepository
      include Domain::Invitation::AuthRepository

      # @param cfg [ConfigLoader] 設定
      def initialize(cfg)
        @redis  = RedisClient.new_client(cfg)
        @prefix = cfg.app.cache_prefix
      end

      def store(token, role, ttl)
        @redis.set(cache_key(token), role.to_s, ex: ttl)
        nil
      end

      def find(token)
        value = @redis.get(cache_key(token))
        value&.match?(/\A\d+\z/) ? value.to_i : nil
      end

      def remove(token)
        @redis.del(cache_key(token))
        nil
      end

      private

      def cache_key(token)
        "#{@prefix}:invitation_auth:invitation_auth:#{token}"
      end
    end
  end
end
