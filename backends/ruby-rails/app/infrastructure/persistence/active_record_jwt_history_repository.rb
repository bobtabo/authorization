# frozen_string_literal: true
#
# JWT 履歴リポジトリの ActiveRecord 実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ActiveRecord を用いた JWT 履歴リポジトリの実装クラスです。
    class ActiveRecordJwtHistoryRepository
      def initialize
        @model = Infrastructure::Model::JwtHistory
      end

      def find_by_client_id(client_id)
        @model
          .where(client_id: client_id, deleted_at: nil)
          .order(issue_at: :desc)
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
