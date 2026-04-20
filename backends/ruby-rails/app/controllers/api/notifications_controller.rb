class Api::NotificationsController < Api::BaseController
  def counts
    staff_id = staff_id_from_cookie
    return render json: { error: "unauthenticated" }, status: :unauthorized if staff_id == 0

    unread, total = container[:notification_uc].counts(staff_id)
    render json: { unread: unread, total: total }
  end

  def index
    staff_id = staff_id_from_cookie
    return render json: { error: "unauthenticated" }, status: :unauthorized if staff_id == 0

    cursor = params[:cursor]
    limit  = params[:limit]&.to_i || container[:cfg].app.notification_default_limit
    page   = container[:notification_uc].list_page(staff_id, cursor, limit)
    render json: {
      items:       page.items.map { |n| UseCase::Notification::Interactor.map_notification(n) },
      next_cursor: page.next_cursor,
    }
  end

  def read_all
    staff_id = staff_id_from_cookie
    return render json: { error: "unauthenticated" }, status: :unauthorized if staff_id == 0

    updated = container[:notification_uc].bulk_mark_read(staff_id)
    render json: { updated: updated }
  end

  def read
    container[:notification_uc].mark_read(params[:id].to_i)
    render json: { id: params[:id].to_i }
  end
end
