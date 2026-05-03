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
        @repo.find_by_condition(cond)
      end

      def update_role(dto)
        @repo.update_role(dto.id, dto.role, dto.executor_id, dto.version)
        nil
      end

      def restore(dto)
        @repo.restore(dto.id, dto.version)
        nil
      end

      def destroy(dto)
        @repo.soft_delete(dto.id, dto.executor_id, dto.version)
        nil
      end
    end
  end
end
