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

      # @param identifier [String] 識別子
      # @param member_id [String] メンバー ID
      # @return [String, nil] JWT トークン
      def get_jwt(identifier, member_id) = raise NotImplementedError

      # @param identifier [String] 識別子
      # @param member_id [String] メンバー ID
      # @param token [String] JWT トークン
      # @param ttl [Integer] 有効期限（秒）
      # @return [void]
      def put_jwt(identifier, member_id, token, ttl) = raise NotImplementedError
    end
  end
end
