# frozen_string_literal: true
#
# 通知 API コントローラー。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

# 通知に関する API エンドポイントを提供するコントローラーです。
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
class Api::NotificationsController < Api::BaseController
  # 未読件数と総件数を返します。
  def counts
    staff_id = staff_id_from_cookie
    return render json: { error: "unauthenticated" }, status: :unauthorized if staff_id == 0

    v = container[:notification_uc].counts(staff_id)
    render json: { unread: v.unread, total: v.total }
  end

  # 通知一覧をページネーションで返します。
  def index
    staff_id = staff_id_from_cookie
    return render json: { error: "unauthenticated" }, status: :unauthorized if staff_id == 0

    cursor = params[:cursor]
    limit  = params[:limit]&.to_i || container[:cfg].app.notification_default_limit
    page   = container[:notification_uc].list_page(staff_id, cursor, limit)
    render json: {
      items:       page.items.map { |n|
        {
          id:           n.id,
          staff_id:     n.staff_id,
          message_type: n.message_type,
          title:        n.title,
          message:      n.message,
          url:          n.url,
          read:         n.read,
          created_at:   n.created_at.strftime(TIME_FORMAT),
          updated_at:   n.updated_at.strftime(TIME_FORMAT),
        }
      },
      next_cursor: page.next_cursor,
    }
  end

  # 全通知を既読にします。
  def read_all
    staff_id = staff_id_from_cookie
    return render json: { error: "unauthenticated" }, status: :unauthorized if staff_id == 0

    ActiveRecord::Base.transaction do
      container[:notification_uc].bulk_mark_read(staff_id)
    end
    render json: {}
  end

  # 指定通知を既読にします。
  def read
    ActiveRecord::Base.transaction do
      container[:notification_uc].mark_read(params[:id].to_i)
    end
    render json: { id: params[:id].to_i }
  end
end
