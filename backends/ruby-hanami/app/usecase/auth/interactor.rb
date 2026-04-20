module UseCase
  module Auth
    class Interactor
      def initialize(staff_repo)
        @staff_repo = staff_repo
      end

      def find_user(id)  = raise NotImplementedError
      def login(dto)     = raise NotImplementedError
    end
  end
end
