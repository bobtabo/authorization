module Authorization
  module Actions
    module Auth
      class GoogleCallback < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          json_response(response, {})
        end
      end
    end
  end
end
