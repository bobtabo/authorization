# frozen_string_literal: true
#
# クライアントリポジトリインターフェースを定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Client
    # クライアントリポジトリのインターフェースです。
    module Repository
      # @param cond [Domain::Client::Condition] 検索条件
      # @return [Array<Domain::Client::Entity>] クライアントエンティティの配列
      def find_by_condition(cond)      = raise NotImplementedError

      # @param cond [Domain::Client::Condition] 検索条件
      # @return [Integer] 検索条件に合致する総件数
      def count_by_condition(cond)     = raise NotImplementedError

      # @param id [Integer] クライアント ID
      # @return [Domain::Client::Entity] クライアントエンティティ
      def find_by_id(id)               = raise NotImplementedError

      # @param token [String] アクセストークン
      # @return [Domain::Client::Entity, nil] クライアントエンティティ
      def find_by_access_token(token)  = raise NotImplementedError

      # @param ident [String] 識別子
      # @return [Domain::Client::Entity, nil] クライアントエンティティ
      def find_by_identifier(ident)    = raise NotImplementedError

      # @param entity [Domain::Client::Entity] 保存するエンティティ
      # @return [Domain::Client::Entity] 保存後のエンティティ
      def save(entity)                 = raise NotImplementedError

      # @param id [Integer] クライアント ID
      # @param deleted_by [Integer] 削除者 ID
      # @param version [Integer] 削除対象の期待バージョン
      # @return [Boolean] 削除成否
      def soft_delete(id, deleted_by, version)  = raise NotImplementedError
    end
  end
end
