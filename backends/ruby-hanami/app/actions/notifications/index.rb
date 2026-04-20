module Authorization
  module Actions
    module Notifications
      class Index < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          staff_id = staff_id_from_cookie(request)
          return json_response(response, { error: "unauthenticated" }, status: 401) if staff_id == 0

          cursor = request.params[:cursor]
          limit  = request.params[:limit]&.to_i || container[:cfg].app.notification_default_limit
          page   = container[:notification_uc].list_page(staff_id, cursor, limit)
          json_response(response, {
            items:       page.items.map { |n| UseCase::Notification::Interactor.map_notification(n) },
            next_cursor: page.next_cursor,
          })
        end
      end
    end
  end
end
