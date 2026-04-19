module Authorization
  module Actions
    module Notifications
      class Read < Authorization::Action
        def handle(request, response)
          response.status = 204
        end
      end
    end
  end
end
