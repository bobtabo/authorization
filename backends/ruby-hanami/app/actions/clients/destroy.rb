module Authorization
  module Actions
    module Clients
      class Destroy < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          executor_id = staff_id_from_cookie(request)
          container[:client_uc].destroy(request.params[:id].to_i, executor_id)
          json_response(response, {})
        end
      end
    end
  end
end
