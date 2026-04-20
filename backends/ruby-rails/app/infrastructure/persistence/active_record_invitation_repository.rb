module Infrastructure
  module Persistence
    class ActiveRecordInvitationRepository
      def initialize
        @model = Infrastructure::Model::Invitation
      end
    end
  end
end
