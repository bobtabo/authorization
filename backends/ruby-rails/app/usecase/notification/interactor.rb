# frozen_string_literal: true
#
# 通知ユースケースの実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Notification
    # 通知に関するユースケースの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param repo [Domain::Notification::Repository] 通知リポジトリ
      # @param staff_repo [Domain::Staff::Repository] スタッフリポジトリ
      def initialize(repo, staff_repo)
        @repo       = repo
        @staff_repo = staff_repo
      end

      def list_page(staff_id, cursor, limit)
        @repo.list_page(staff_id, cursor, limit)
      end

      def counts(staff_id)
        @repo.counts(staff_id)
      end

      def bulk_mark_read(staff_id)
        @repo.bulk_mark_read(staff_id, [], true)
        nil
      end

      def fan_out(dto)
        @staff_repo.find_all_active.each do |staff|
          begin
            @repo.store(
              staff.id,
              dto.message_type,
              dto.title,
              dto.message,
              dto.executor_id,
              dto.url,
            )
          rescue StandardError
            nil
          end
        end
        nil
      end

      def mark_read(id)
        @repo.patch(id, { "read" => true })
        nil
      end
    end
  end
end
