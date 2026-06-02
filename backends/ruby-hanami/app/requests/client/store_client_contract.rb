# frozen_string_literal: true
#
# クライアント登録バリデーション Contract。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "dry/validation"
require "uri"

# クライアント登録リクエストのバリデーションを行う Contract です。
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
class StoreClientContract < Dry::Validation::Contract
  params do
    required(:name).filled(:string)
    required(:post_code).filled(:string)
    required(:pref).filled(:string)
    required(:city).filled(:string)
    required(:address).filled(:string)
    optional(:building).maybe(:string)
    required(:tel).filled(:string)
    required(:email).filled(:string)
  end

  rule(:name)      { key.failure("は255文字以内で入力してください") if value.length > 255 }
  rule(:post_code) { key.failure("は8文字以内で入力してください") if value.length > 8 }
  rule(:pref)      { key.failure("は50文字以内で入力してください") if value.length > 50 }
  rule(:city)      { key.failure("は100文字以内で入力してください") if value.length > 100 }
  rule(:address)   { key.failure("は255文字以内で入力してください") if value.length > 255 }
  rule(:building)  { key.failure("は255文字以内で入力してください") if value && value.length > 255 }
  rule(:tel)       { key.failure("は数字のみ10〜11桁で入力してください") unless /\A\d{10,11}\z/.match?(value) }
  rule(:email) do
    key.failure("は255文字以内で入力してください") if value.length > 255
    key.failure("は正しいメールアドレス形式で入力してください") unless URI::MailTo::EMAIL_REGEXP.match?(value)
  end
end
