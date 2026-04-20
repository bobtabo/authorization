module Authorization
  module Actions
    module Notifications
      class Counts < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          staff_id = staff_id_from_cookie(request)
          return json_response(response, { error: "unauthenticated" }, status: 401) if staff_id == 0

          unread, total = container[:notification_uc].counts(staff_id)
          json_response(response, { unread: unread, total: total })
        end
      end
    end
  end
end
