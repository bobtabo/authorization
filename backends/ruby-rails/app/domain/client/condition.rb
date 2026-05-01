# frozen_string_literal: true
#
# クライアント検索条件を表すドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Client
    # クライアント一覧取得の検索条件です。
    Condition = Struct.new(:keyword, :start_from, :start_to, :statuses, keyword_init: true)
  end
end
