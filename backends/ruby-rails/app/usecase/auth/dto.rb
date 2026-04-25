# frozen_string_literal: true
#
# 認証ユースケースの DTO を定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Auth
    # ログイン時に渡す DTO です。
    LoginDto = Struct.new(:provider, :provider_id, :name, :email, :avatar, keyword_init: true)
  end
end
