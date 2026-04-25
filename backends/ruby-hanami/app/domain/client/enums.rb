# frozen_string_literal: true
#
# クライアントのステータス定数を定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Client
    # クライアントのステータス定数です。
    module Status
      INACTIVE  = 1
      ACTIVE    = 2
      SUSPENDED = 3
      CLOSED    = 4
    end
  end
end
