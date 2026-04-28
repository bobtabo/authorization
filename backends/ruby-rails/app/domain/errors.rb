# frozen_string_literal: true
#
# ドメイン共通エラー定義モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  # HTTP 403 Forbidden に対応するアプリケーション例外です。
  class ForbiddenError < StandardError
    attr_reader :code

    def initialize(msg = "forbidden")
      super(msg)
      @code = 403
    end
  end
end
