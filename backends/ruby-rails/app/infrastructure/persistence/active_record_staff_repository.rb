module Infrastructure
  module Persistence
    class ActiveRecordStaffRepository
      def initialize
        @model = Infrastructure::Model::Staff
      end
    end
  end
end
