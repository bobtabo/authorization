# frozen_string_literal: true
#
# ゲートの値オブジェクトとキャッシュリポジトリインターフェースを定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Gate
    # トークン発行結果の値オブジェクトです。
    IssueVo  = Struct.new(:token, keyword_init: true)

    # トークン検証結果の値オブジェクトです。
    VerifyVo = Struct.new(:claims, keyword_init: true)

    # ゲートキャッシュリポジトリのインターフェースです。
    module CacheRepository
      # @param identifier [String] 識別子
      # @param member_id [String] メンバー ID
      # @return [String, nil] JWT トークン
      def get_jwt(identifier, member_id)              = raise NotImplementedError

      # @param identifier [String] 識別子
      # @param member_id [String] メンバー ID
      # @param token [String] JWT トークン
      # @param ttl [Integer] 有効期限（秒）
      # @return [void]
      def put_jwt(identifier, member_id, token, ttl)  = raise NotImplementedError
    end
  end
end
