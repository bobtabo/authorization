# frozen_string_literal: true
#
# スタッフユースケースのインターフェースを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Staff
    # スタッフに関するユースケースのインターフェースです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param repo [Domain::Staff::Repository] スタッフリポジトリ
      def initialize(repo)
        @repo = repo
      end

      # @param cond [Domain::Staff::Condition] 検索条件
      # @return [Array<Domain::Staff::ListItem>] スタッフ一覧
      def find_by_condition(cond) = raise NotImplementedError

      # @param dto [UseCase::Staff::UpdateRoleDto] ロール更新 DTO
      # @return [void]
      def update_role(dto)        = raise NotImplementedError

      # @param id [Integer] スタッフ ID
      # @return [void]
      def restore(id)             = raise NotImplementedError

      # @param dto [UseCase::Staff::DestroyDto] 削除 DTO
      # @return [void]
      def destroy(dto)            = raise NotImplementedError
    end
  end
end
