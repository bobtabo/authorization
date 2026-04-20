module Domain
  module Notification
    module Repository
      def list_page(staff_id, cursor, limit)                                     = raise NotImplementedError
      def counts(staff_id)                                                       = raise NotImplementedError
      def bulk_mark_read(staff_id, ids, all)                                     = raise NotImplementedError
      def store(staff_id, message_type, title, message, created_by, url: nil)   = raise NotImplementedError
      def patch(id, attrs)                                                       = raise NotImplementedError
    end
  end
end
