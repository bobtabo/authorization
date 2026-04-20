module Domain
  module Notification
    Page = Struct.new(:items, :next_cursor, keyword_init: true)
  end
end
