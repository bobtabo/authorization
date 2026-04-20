module Domain
  module Invitation
    module Repository
      def get_current             = raise NotImplementedError
      def issue                   = raise NotImplementedError
      def find_by_token(token)    = raise NotImplementedError
    end
  end
end
