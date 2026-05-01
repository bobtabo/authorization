# frozen_string_literal: true
#
# スタッフの値オブジェクトを定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Staff
    # スタッフ一覧の表示用値オブジェクトです。
    ListItem = Struct.new(:id, :name, :email, :role, :status, :created_at, :updated_at, keyword_init: true)

    # スタッフ詳細の値オブジェクトです。
    Vo = Struct.new(:id, :name, :avatar, :role, keyword_init: true)
  end
end
