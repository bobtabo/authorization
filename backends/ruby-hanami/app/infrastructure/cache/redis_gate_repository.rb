# frozen_string_literal: true
#
# Redis を用いたゲートリポジトリを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Cache
    # Redis を使ってゲートJWTを保管するリポジトリです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class RedisGateRepository
      # @param cfg [ConfigLoader] 設定
      def initialize(cfg)
        @redis  = RedisClient.new_client(cfg)
        @prefix = cfg.app.cache_prefix
      end

      # @param identifier [String] クライアント識別子
      # @param member_id [String] メンバー ID
      # @return [String, nil] キャッシュされた JWT
      def get_jwt(identifier, member_id) = raise NotImplementedError

      # @param identifier [String] クライアント識別子
      # @param member_id [String] メンバー ID
      # @param token [String] JWT
      # @param ttl [Integer] TTL 秒数
      # @return [void]
      def put_jwt(identifier, member_id, token, ttl) = raise NotImplementedError
    end
  end
end
