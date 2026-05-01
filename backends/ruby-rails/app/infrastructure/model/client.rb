# frozen_string_literal: true
#
# クライアントモデルを定義するインフラストラクチャモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Model
    # clients テーブルに対応する ActiveRecord モデルです。
    class Client < ActiveRecord::Base
      self.table_name = "clients"
    end
  end
end
