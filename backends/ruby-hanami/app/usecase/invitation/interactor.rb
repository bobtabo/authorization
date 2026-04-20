module UseCase
  module Invitation
    class Interactor
      def initialize(repo)
        @repo = repo
      end

      def current            = raise NotImplementedError
      def issue              = raise NotImplementedError
      def find_by_token(dto) = raise NotImplementedError
    end
  end
end
