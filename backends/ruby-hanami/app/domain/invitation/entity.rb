# frozen_string_literal: true
#
# 招待エンティティを表すドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Invitation
    # 招待のエンティティです。
    Entity = Struct.new(
      :id, :token, :role,
      :created_at, :created_by, :updated_at, :updated_by,
      :deleted_at, :deleted_by, :version,
      keyword_init: true
    )
  end
end
