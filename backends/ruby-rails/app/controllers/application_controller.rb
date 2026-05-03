# frozen_string_literal: true
#
# アプリケーション基底コントローラー。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

# アプリケーション全体の基底コントローラーです。
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
class ApplicationController < ActionController::API
  include ActionController::Cookies

  rescue_from Domain::ConflictError do |e|
    render json: { error: e.message }, status: :conflict
  end
end
