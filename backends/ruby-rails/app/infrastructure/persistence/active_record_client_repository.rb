module Infrastructure
  module Persistence
    class ActiveRecordClientRepository
      def initialize
        @model = Infrastructure::Model::Client
      end
    end
  end
end
