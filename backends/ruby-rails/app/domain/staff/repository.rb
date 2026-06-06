# frozen_string_literal: true
#
# スタッフリポジトリインターフェースを定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Staff
    # スタッフリポジトリのインターフェースです。
    module Repository
      # @param cond [Domain::Staff::Condition] 検索条件
      # @return [Integer] 総件数
      def count_by_condition(cond)                  = raise NotImplementedError

      # @param cond [Domain::Staff::Condition] 検索条件
      # @return [Array<Domain::Staff::Entity>] スタッフエンティティの配列
      def find_by_condition(cond)                   = raise NotImplementedError

      # @param id [Integer] スタッフ ID
      # @return [Domain::Staff::Entity] スタッフエンティティ
      def find_by_id(id)                            = raise NotImplementedError

      # @param provider [Integer] プロバイダー種別
      # @param provider_id [String] プロバイダー ID
      # @return [Domain::Staff::Entity, nil] スタッフエンティティ
      def find_by_provider(provider, provider_id)   = raise NotImplementedError

      # @return [Array<Domain::Staff::Entity>] アクティブなスタッフエンティティの配列
      def find_all_active                            = raise NotImplementedError

      # @param entity [Domain::Staff::Entity] 保存するエンティティ
      # @return [Domain::Staff::Entity] 保存後のエンティティ
      def save(entity)                              = raise NotImplementedError

      # @param id [Integer] スタッフ ID
      # @param role [Integer] 新しいロール
      # @param updated_by [Integer] 更新者 ID
      # @return [void]
      def update_role(id, role, updated_by)         = raise NotImplementedError

      # @param id [Integer] スタッフ ID
      # @param deleted_by [Integer] 削除者 ID
      # @param version [Integer] 削除対象の期待バージョン
      # @return [Boolean] 削除成否
      def soft_delete(id, deleted_by, version)      = raise NotImplementedError

      # @param id [Integer] スタッフ ID
      # @return [void]
      def restore(id)                               = raise NotImplementedError
    end
  end
end
