# frozen_string_literal: true
#
# 管理者向け招待 API コントローラー。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

# 管理者向け招待に関する API エンドポイントを提供するコントローラーです。
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
class Api::Admin::InvitationsController < Api::BaseController
  # 現在有効な招待情報を返します。
  def index
    role = resolve_role
    return if performed?
    v = container[:invitation_uc].current(role)
    render json: { found: true, url: v.url, display_url: v.display_url, token: v.token }
  end

  # 新しい招待を発行します。
  def issue
    if staff_id_from_cookie == 0
      return render json: { error: "unauthenticated" }, status: :unauthorized
    end

    role = resolve_role
    return if performed?

    v = ActiveRecord::Base.transaction do
      container[:invitation_uc].issue(role)
    end
    render json: { found: true, url: v.url, display_url: v.display_url, token: v.token }
  end

  private

  # クエリパラメーターから role を取得します（1 or 2、それ以外は 400）。
  # @return [Integer] 権限（1=管理者, 2=メンバー）
  def resolve_role
    role = params[:role].presence ? params[:role].to_i : 2
    unless [1, 2].include?(role)
      render json: { error: "invalid_role" }, status: :bad_request
      return
    end
    role
  end
end
