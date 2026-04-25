# frozen_string_literal: true
#
# 通知エンティティを表すドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Notification
    # 通知のエンティティです。
    Entity = Struct.new(
      :id, :staff_id, :message_type, :title, :message, :url, :read,
      :created_at, :created_by, :updated_at, :updated_by,
      :deleted_at, :deleted_by, :version,
      keyword_init: true
    )
  end
end
