module Infrastructure
  module Persistence
    class ActiveRecordNotificationRepository
      def initialize
        @model = Infrastructure::Model::Notification
      end
    end
  end
end
