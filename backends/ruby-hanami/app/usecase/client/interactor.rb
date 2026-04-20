module UseCase
  module Client
    class Interactor
      def initialize(repo)
        @repo = repo
      end

      def find_by_condition(dto) = raise NotImplementedError
      def find_by_id(id)         = raise NotImplementedError
      def store(dto)             = raise NotImplementedError
      def update(dto)            = raise NotImplementedError
      def destroy(id, executor_id) = raise NotImplementedError
      def find_by_access_token(token) = raise NotImplementedError
      def find_by_identifier(identifier) = raise NotImplementedError
    end
  end
end
