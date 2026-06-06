# frozen_string_literal: true
#
# スタッフ検索条件を表すドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Staff
    # スタッフ一覧取得の検索条件です。
    Condition = Struct.new(:keyword, :roles, :offset, :limit, :sort, :sort_type, keyword_init: true)
  end
end
