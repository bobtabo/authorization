module Authorization
  module Actions
    module Auth
      class Logout < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          json_response(response, {})
        end
      end
    end
  end
end
