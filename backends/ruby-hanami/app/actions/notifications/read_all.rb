module Authorization
  module Actions
    module Notifications
      class ReadAll < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          staff_id = staff_id_from_cookie(request)
          return json_response(response, { error: "unauthenticated" }, status: 401) if staff_id == 0

          updated = container[:notification_uc].bulk_mark_read(staff_id)
          json_response(response, { updated: updated })
        end
      end
    end
  end
end
