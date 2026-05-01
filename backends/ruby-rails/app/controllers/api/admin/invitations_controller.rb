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
    v = container[:invitation_uc].current
    render json: { found: true, url: v.url, display_url: v.display_url, token: v.token }
  end

  # 新しい招待を発行します。
  def issue
    v = ActiveRecord::Base.transaction do
      container[:invitation_uc].issue
    end
    render json: { found: true, url: v.url, display_url: v.display_url, token: v.token }
  end
end
