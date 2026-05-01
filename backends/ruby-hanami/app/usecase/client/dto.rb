# frozen_string_literal: true
#
# クライアントユースケースの DTO を定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Client
    # クライアント登録時に渡す DTO です。
    StoreDto = Struct.new(
      :name, :post_code, :pref, :city, :address, :building, :tel, :email, :executor_id,
      keyword_init: true
    )

    # クライアント更新時に渡す DTO です。
    UpdateDto = Struct.new(
      :id, :name, :post_code, :pref, :city, :address, :building, :tel, :email, :status, :executor_id,
      keyword_init: true
    )

    # クライアント一覧取得時に渡す DTO です。
    ListConditionDto = Struct.new(:keyword, :start_from, :start_to, :statuses, keyword_init: true)
  end
end
