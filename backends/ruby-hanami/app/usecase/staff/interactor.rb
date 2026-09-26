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

      def find_by_condition(cond)
        count = @repo.count_by_condition(cond)
        items = @repo.find_by_condition(cond)
        { items: items, count: count }
      end

      def update_role(dto)
        raise Domain::UnauthorizedError if dto.executor_id.zero?

        # 無効化（論理削除）はログイン可否にのみ影響するため、実行者チェックも無効スタッフを含めて見る。
        executor = @repo.find_by_id(dto.executor_id)
        raise Domain::ForbiddenError if executor.nil? || !executor.deleted_at.nil? || executor.role != Domain::Staff::Role::ADMIN

        @repo.update_role(dto.id, dto.role, dto.executor_id, dto.version)
        nil
      end

      def restore(dto)
        @repo.restore(dto.id)
        nil
      end

      def destroy(dto)
        @repo.soft_delete(dto.id, dto.executor_id, dto.version)
        nil
      end
    end
  end
end
