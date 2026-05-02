# frozen_string_literal: true
#
# スタッフのロールおよびプロバイダー定数を定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Staff
    # スタッフのロール定数です。
    module Role
      ADMIN  = 1
      MEMBER = 2
    end

    # 認証プロバイダーの定数です。
    module Provider
      GOOGLE = 1
      GITHUB = 2
    end
  end
end
