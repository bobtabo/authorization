# frozen_string_literal: true
#
# 通知の値オブジェクトを定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Notification
    # 通知ページネーション結果の値オブジェクトです。
    Page = Struct.new(:items, :next_cursor, keyword_init: true)

    # 通知件数の値オブジェクトです。
    CountsVo = Struct.new(:unread, :total, keyword_init: true)
  end
end
