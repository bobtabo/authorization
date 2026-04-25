# frozen_string_literal: true
#
# ゲートユースケースの DTO を定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Gate
    # トークン発行時に渡す DTO です。
    IssueDto  = Struct.new(:access_token, :member_id, keyword_init: true)

    # トークン検証時に渡す DTO です。
    VerifyDto = Struct.new(:identifier, :token, keyword_init: true)
  end
end
