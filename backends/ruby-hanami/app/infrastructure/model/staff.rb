# frozen_string_literal: true
#
# スタッフモデルを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Model
    # スタッフの永続化モデルです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    Staff = Struct.new(
      :id, :name, :email, :provider, :provider_id, :avatar, :role, :last_login_at,
      :created_at, :created_by, :updated_at, :updated_by,
      :deleted_at, :deleted_by, :version,
      keyword_init: true
    )
  end
end
