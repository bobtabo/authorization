# frozen_string_literal: true
#
# クライアント更新バリデーション Contract。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "dry/validation"
require "uri"

# クライアント更新リクエストのバリデーションを行う Contract です。
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
module Client
class UpdateClientContract < Dry::Validation::Contract
  params do
    optional(:name).maybe(:string)
    optional(:post_code).maybe(:string)
    optional(:pref).maybe(:string)
    optional(:city).maybe(:string)
    optional(:address).maybe(:string)
    optional(:building).maybe(:string)
    optional(:tel).maybe(:string)
    optional(:email).maybe(:string)
    optional(:status).maybe(:integer)
    required(:version).filled(:integer)
  end

  rule(:name)      { key.failure("は255文字以内で入力してください") if value && value.length > 255 }
  rule(:post_code) { key.failure("は8文字以内で入力してください") if value && value.length > 8 }
  rule(:pref)      { key.failure("は50文字以内で入力してください") if value && value.length > 50 }
  rule(:city)      { key.failure("は100文字以内で入力してください") if value && value.length > 100 }
  rule(:address)   { key.failure("は255文字以内で入力してください") if value && value.length > 255 }
  rule(:building)  { key.failure("は255文字以内で入力してください") if value && value.length > 255 }
  rule(:tel)       { key.failure("は数字のみ10〜11桁で入力してください") if value && !/\A\d{10,11}\z/.match?(value) }
  rule(:email) do
    if value
      key.failure("は255文字以内で入力してください") if value.length > 255
      key.failure("は正しいメールアドレス形式で入力してください") unless URI::MailTo::EMAIL_REGEXP.match?(value)
    end
  end
end
end
