module UseCase
  module Notification
    class Interactor
      def initialize(repo, staff_repo)
        @repo       = repo
        @staff_repo = staff_repo
      end

      def list_page(staff_id, cursor, limit) = raise NotImplementedError
      def counts(staff_id)                   = raise NotImplementedError
      def bulk_mark_read(staff_id)           = raise NotImplementedError
      def fan_out(dto)                       = raise NotImplementedError
      def mark_read(id)                      = raise NotImplementedError

      def self.map_notification(n)
        {
          id:           n.id,
          staff_id:     n.staff_id,
          message_type: n.message_type,
          title:        n.title,
          message:      n.message,
          url:          n.url,
          read:         n.read,
          created_at:   n.created_at.strftime("%Y-%m-%d %H:%M"),
          updated_at:   n.updated_at.strftime("%Y-%m-%d %H:%M"),
        }
      end
    end
  end
end
