# frozen_string_literal: true
#
# 招待認証リポジトリインターフェースモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Invitation
    # 招待認証トークンのキャッシュリポジトリインターフェースです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    module AuthRepository
      # @param token [String] 招待トークン
      # @param ttl [Integer] キャッシュ有効期間（秒）
      def store(token, ttl) = raise NotImplementedError
      # @param token [String] 招待トークン
      # @return [String, nil] トークン文字列、または nil
      def find(token) = raise NotImplementedError
      # @param token [String] 招待トークン
      def remove(token) = raise NotImplementedError
    end
  end
end
