module Authorization
  module Actions
    module Notifications
      class ReadAll < Authorization::Action
        def handle(request, response)
          response.status = 204
        end
      end
    end
  end
end
