module Authorization
  module Actions
    module Admin
      module Invitations
        class Index < Authorization::Action
          def handle(request, response)
            response.status = 200
          end
        end
      end
    end
  end
end
