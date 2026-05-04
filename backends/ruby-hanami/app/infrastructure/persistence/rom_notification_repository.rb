# frozen_string_literal: true

require "base64"

module Infrastructure
  module Persistence
    class RomNotificationRepository
      def initialize(rom)
        @ds = rom.gateways[:default].connection[:notifications]
      end

      def list_page(staff_id, cursor, limit)
        q = @ds.where(staff_id: staff_id, deleted_at: nil)

        if cursor
          dt, cur_id = decode_cursor(cursor)
          raise "invalid_cursor" unless dt
          q = q.where(
            Sequel.|(
              Sequel.lit("created_at < ?", dt),
              Sequel.lit("created_at = ? AND id < ?", dt, cur_id),
            )
          )
        end

        rows     = q.order(Sequel.desc(:created_at), Sequel.desc(:id)).limit(limit + 1).all
        has_next = rows.size > limit
        items    = rows.first(limit).map { |r| row_to_entity(r) }
        next_cursor = has_next ? encode_cursor(items.last.created_at, items.last.id) : nil

        Domain::Notification::Page.new(items: items, next_cursor: next_cursor)
      end

      def counts(staff_id)
        base   = @ds.where(staff_id: staff_id, deleted_at: nil)
        total  = base.count
        unread = base.where(read: false).count
        Domain::Notification::CountsVo.new(unread: unread, total: total)
      end

      def bulk_mark_read(staff_id, ids, all)
        now = Time.now
        if all
          @ds.where(staff_id: staff_id, read: false, deleted_at: nil)
             .update(read: true, updated_at: now)
        else
          return 0 if ids.empty?
          @ds.where(staff_id: staff_id, id: ids, read: false, deleted_at: nil)
             .update(read: true, updated_at: now)
        end
      end

      def store(staff_id, message_type, title, message, created_by, url)
        now = Time.now
        @ds.insert(
          staff_id:     staff_id,
          message_type: message_type,
          title:        title,
          message:      message,
          url:          url,
          read:         false,
          created_at:   now,
          created_by:   created_by,
          updated_at:   now,
          updated_by:   created_by,
          version:      1,
        )
        nil
      end

      def patch(id, attrs)
        now = Time.now
        updates = {}
        updates[:read] = attrs["read"] if attrs.key?("read")
        updates[:updated_at] = now
        @ds.where(id: id).update(updates) > 0
      end

      private

      def row_to_entity(r)
        Support::Assign.call(Domain::Notification::Entity.new, r)
      end

      def encode_cursor(created_at, id)
        Base64.strict_encode64("#{created_at.to_i},#{id}")
      end

      def decode_cursor(cursor)
        raw   = Base64.strict_decode64(cursor)
        parts = raw.split(",", 2)
        [Time.at(parts[0].to_i).utc, parts[1].to_i]
      rescue StandardError
        nil
      end
    end
  end
end
