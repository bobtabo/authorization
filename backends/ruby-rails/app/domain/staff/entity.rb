# frozen_string_literal: true
#
# スタッフエンティティを表すドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Staff
    # スタッフのエンティティです。
    Entity = Struct.new(
      :id, :name, :email, :provider, :provider_id, :avatar, :role, :last_login_at,
      :created_at, :created_by, :updated_at, :updated_by,
      :deleted_at, :deleted_by, :version,
      keyword_init: true
    )
  end
end
