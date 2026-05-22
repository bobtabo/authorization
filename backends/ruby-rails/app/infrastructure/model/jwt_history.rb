# frozen_string_literal: true
#
# JWT 履歴モデルを定義するインフラストラクチャモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Model
    # jwt_histories テーブルに対応する ActiveRecord モデルです。
    class JwtHistory < ActiveRecord::Base
      self.table_name = "jwt_histories"
    end
  end
end
