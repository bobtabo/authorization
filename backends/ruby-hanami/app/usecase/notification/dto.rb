module UseCase
  module Notification
    FanOutDto = Struct.new(:title, :message, :message_type, :executor_id, :url, keyword_init: true)
  end
end
