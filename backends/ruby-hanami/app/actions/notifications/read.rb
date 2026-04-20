module Authorization
  module Actions
    module Notifications
      class Read < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          id = request.params[:id].to_i
          container[:notification_uc].mark_read(id)
          json_response(response, { id: id })
        end
      end
    end
  end
end
