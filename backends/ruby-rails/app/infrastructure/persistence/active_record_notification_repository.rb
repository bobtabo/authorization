# frozen_string_literal: true
#
# 通知リポジトリの ActiveRecord 実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "base64"

module Infrastructure
  module Persistence
    # ActiveRecord を用いた通知リポジトリの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class ActiveRecordNotificationRepository
      def initialize
        @model = Infrastructure::Model::Notification
      end

      def list_page(staff_id, cursor, limit)
        q = @model.where(staff_id: staff_id, deleted_at: nil)

        if cursor
          dt, cur_id = decode_cursor(cursor)
          raise "invalid_cursor" unless dt
          q = q.where(
            "(created_at < ?) OR (created_at = ? AND id < ?)",
            dt, dt, cur_id
          )
        end

        rows     = q.order(created_at: :desc, id: :desc).limit(limit + 1).to_a
        has_next = rows.size > limit
        items    = rows.first(limit).map { |r| row_to_entity(r) }
        next_cursor = has_next ? encode_cursor(items.last.created_at, items.last.id) : nil

        Domain::Notification::Page.new(items: items, next_cursor: next_cursor)
      end

      def counts(staff_id)
        base  = @model.where(staff_id: staff_id, deleted_at: nil)
        total  = base.count
        unread = base.where(read: false).count
        Domain::Notification::CountsVo.new(unread: unread, total: total)
      end

      def bulk_mark_read(staff_id, ids, all)
        now = Time.current
        if all
          @model.where(staff_id: staff_id, read: false, deleted_at: nil)
                .update_all(read: true, updated_at: now)
        else
          return 0 if ids.empty?
          @model.where(staff_id: staff_id, id: ids, read: false, deleted_at: nil)
                .update_all(read: true, updated_at: now)
        end
      end

      def store(staff_id, message_type, title, message, created_by, url)
        now = Time.current
        @model.create!(
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
        now = Time.current
        updates = attrs.slice("read").merge(updated_at: now)
        @model.where(id: id).update_all(updates) > 0
      end

      private

      def row_to_entity(r)
        Domain::Notification::Entity.new(
          id:           r.id,
          staff_id:     r.staff_id,
          message_type: r.message_type,
          title:        r.title,
          message:      r.message,
          url:          r.url,
          read:         r.read,
          created_at:   r.created_at,
          created_by:   r.created_by,
          updated_at:   r.updated_at,
          updated_by:   r.updated_by,
          deleted_at:   r.deleted_at,
          deleted_by:   r.respond_to?(:deleted_by) ? r.deleted_by : nil,
          version:      r.version,
        )
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
