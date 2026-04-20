module Authorization
  module Actions
    module Staffs
      class Restore < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          id = request.params[:id].to_i
          container[:staff_uc].restore(id)
          json_response(response, { id: id })
        end
      end
    end
  end
end
