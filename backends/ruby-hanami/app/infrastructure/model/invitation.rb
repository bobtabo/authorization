# frozen_string_literal: true
#
# 招待モデルを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Model
    # 招待の永続化モデルです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    Invitation = Struct.new(
      :id, :token,
      :created_at, :created_by, :updated_at, :updated_by,
      :deleted_at, :deleted_by, :version,
      keyword_init: true
    )
  end
end
