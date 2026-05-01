# frozen_string_literal: true
#
# 招待ユースケースの DTO を定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Invitation
    # トークンによる招待検索時に渡す DTO です。
    FindByTokenDto = Struct.new(:token, keyword_init: true)
  end
end
