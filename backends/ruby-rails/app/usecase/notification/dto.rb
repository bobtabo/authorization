# frozen_string_literal: true
#
# 通知ユースケースの DTO を定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Notification
    # 通知一斉配信時に渡す DTO です。
    FanOutDto = Struct.new(:title, :message, :message_type, :executor_id, :url, keyword_init: true)
  end
end
