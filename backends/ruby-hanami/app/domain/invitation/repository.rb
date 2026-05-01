# frozen_string_literal: true
#
# 招待リポジトリインターフェースを定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Invitation
    # 招待リポジトリのインターフェースです。
    module Repository
      # @return [Domain::Invitation::Entity, nil] 現在有効な招待エンティティ
      def get_current             = raise NotImplementedError

      # @return [Domain::Invitation::Entity] 発行した招待エンティティ
      def issue                   = raise NotImplementedError

      # @param token [String] 招待トークン
      # @return [Domain::Invitation::Entity, nil] 招待エンティティ
      def find_by_token(token)    = raise NotImplementedError
    end
  end
end
