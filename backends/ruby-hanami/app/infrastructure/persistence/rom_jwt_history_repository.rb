# frozen_string_literal: true

module Infrastructure
  module Persistence
    class RomJwtHistoryRepository
      ALLOWED_SORT = %w[issue_at member_id].freeze

      def initialize(rom)
        @ds = rom.gateways[:default].connection[:jwt_histories]
      end

      def count_by_client_id(client_id)
        @ds.where(client_id: client_id, deleted_at: nil).count
      end

      def find_by_condition(client_id, offset: 0, limit: 10, sort: "issue_at", sort_type: "desc")
        sort_col  = ALLOWED_SORT.include?(sort.to_s) ? sort.to_s.to_sym : :issue_at
        direction = sort_type.to_s.downcase == "asc" ? :asc : :desc
        order_expr = direction == :asc ? Sequel.asc(sort_col) : Sequel.desc(sort_col)
        @ds
          .where(client_id: client_id, deleted_at: nil)
          .order(order_expr)
          .limit([limit.to_i, 1].max)
          .offset(offset.to_i)
          .all
      end

      def save(client_id:, member_id:, issue_at:, jwt:)
        now = Time.now
        @ds.insert(
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
