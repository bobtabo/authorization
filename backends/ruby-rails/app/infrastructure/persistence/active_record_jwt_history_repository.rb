# frozen_string_literal: true
#
# JWT 履歴リポジトリの ActiveRecord 実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ActiveRecord を用いた JWT 履歴リポジトリの実装クラスです。
    class ActiveRecordJwtHistoryRepository
      ALLOWED_SORT = %w[issue_at member_id].freeze

      def initialize
        @model = Infrastructure::Model::JwtHistory
      end

      def count_by_client_id(client_id)
        @model.where(client_id: client_id, deleted_at: nil).count
      end

      def find_by_condition(client_id, offset: 0, limit: 20, sort: "issue_at", sort_type: "desc")
        sort_col  = ALLOWED_SORT.include?(sort.to_s) ? sort.to_s : "issue_at"
        direction = sort_type.to_s.downcase == "asc" ? :asc : :desc
        @model
          .where(client_id: client_id, deleted_at: nil)
          .order(sort_col => direction)
          .limit([limit.to_i, 1].max)
          .offset(offset.to_i)
          .all
      end

      def save(client_id:, member_id:, issue_at:, jwt:)
        now = Time.now
        @model.create!(
          client_id:  client_id,
          member_id:  member_id,
          issue_at:   issue_at,
          jwt:        jwt,
          created_at: now,
          created_by: 0,
          updated_at: now,
          updated_by: 0,
          version:    1,
        )
      end
    end
  end
end
