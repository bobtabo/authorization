# frozen_string_literal: true
#
# 招待リポジトリインターフェースを定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Invitation
    # 招待リポジトリのインターフェースです。
    module Repository
      # @param role [Integer] ロール（1=管理者, 2=メンバー）
      # @return [Domain::Invitation::Entity, nil] 現在有効な招待エンティティ
      def get_current_by_role(role) = raise NotImplementedError

      # @param role [Integer] ロール（1=管理者, 2=メンバー）
      # @return [Domain::Invitation::Entity] 発行した招待エンティティ
      def issue(role)               = raise NotImplementedError

      # @param token [String] 招待トークン
      # @return [Domain::Invitation::Entity, nil] 招待エンティティ
      def find_by_token(token)      = raise NotImplementedError
    end
  end
end
