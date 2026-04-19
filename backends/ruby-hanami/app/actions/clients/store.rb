module Authorization
  module Actions
    module Clients
      class Store < Authorization::Action
        def handle(request, response)
          response.status = 200
        end
      end
    end
  end
end
