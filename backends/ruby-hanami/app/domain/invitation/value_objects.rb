# frozen_string_literal: true
#
# 招待の値オブジェクトを定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Invitation
    # 招待情報の値オブジェクトです。
    Vo = Struct.new(:token, :url, :display_url, keyword_init: true)
  end
end
