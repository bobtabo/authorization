# frozen_string_literal: true
#
# クライアントモデルを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Model
    # クライアントの永続化モデルです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    Client = Struct.new(
      :id, :name, :identifier,
      :post_code, :pref, :city, :address, :building, :tel, :email,
      :access_token, :private_key, :public_key, :fingerprint,
      :status, :start_at, :stop_at,
      :created_at, :created_by, :updated_at, :updated_by,
      :deleted_at, :deleted_by, :version,
      keyword_init: true
    )
  end
end
