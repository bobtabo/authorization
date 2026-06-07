# frozen_string_literal: true
#
# スタッフユースケースの実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Staff
    # スタッフに関するユースケースの実装クラスです。
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
        @repo.update_role(dto.id, dto.role, dto.executor_id)
        nil
      end

      def restore(id)
        @repo.restore(id)
        nil
      end

      def destroy(dto)
        @repo.soft_delete(dto.id, dto.executor_id, dto.version)
        nil
      end
    end
  end
end
