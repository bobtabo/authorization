module Authorization
  module Actions
    module Notifications
      class Counts < Authorization::Action
        def handle(request, response)
          response.status = 200
        end
      end
    end
  end
end
