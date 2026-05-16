# frozen_string_literal: true
#
# ドメイン共通エラー定義モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  # HTTP 400 Bad Request に対応するアプリケーション例外です。
  class BadRequestError < StandardError
    attr_reader :code

    def initialize(msg = "bad_request")
      super(msg)
      @code = 400
    end
  end

  # HTTP 401 Unauthorized に対応するアプリケーション例外です。
  class UnauthorizedError < StandardError
    attr_reader :code

    def initialize(msg = "unauthenticated")
      super(msg)
      @code = 401
    end
  end

  # HTTP 403 Forbidden に対応するアプリケーション例外です。
  class ForbiddenError < StandardError
    attr_reader :code

    def initialize(msg = "forbidden")
      super(msg)
      @code = 403
    end
  end

  # HTTP 409 Conflict に対応する楽観排他エラーです。
  class ConflictError < StandardError
    attr_reader :code

    def initialize(msg = "optimistic lock")
      super(msg)
      @code = 409
    end
  end
end
