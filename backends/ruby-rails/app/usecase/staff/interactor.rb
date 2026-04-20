module UseCase
  module Staff
    class Interactor
      def initialize(repo)
        @repo = repo
      end

      def find_by_condition(cond) = raise NotImplementedError
      def update_role(dto)        = raise NotImplementedError
      def restore(id)             = raise NotImplementedError
      def destroy(dto)            = raise NotImplementedError

      def self.status(s)
        s.deleted_at.nil? ? 1 : 0
      end
    end
  end
end
