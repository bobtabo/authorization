# frozen_string_literal: true
#
# 通知モデルを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Model
    # 通知の永続化モデルです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    Notification = Struct.new(
      :id, :staff_id, :message_type, :title, :message, :url, :read,
      :created_at, :created_by, :updated_at, :updated_by,
      :deleted_at, :deleted_by, :version,
      keyword_init: true
    )
  end
end
