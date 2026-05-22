# frozen_string_literal: true

module Infrastructure
  module Persistence
    class RomJwtHistoryRepository
      def initialize(rom)
        @ds = rom.gateways[:default].connection[:jwt_histories]
      end

      def find_by_client_id(client_id)
        @ds
          .where(client_id: client_id, deleted_at: nil)
          .order(Sequel.desc(:issue_at))
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
