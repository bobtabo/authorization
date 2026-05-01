# frozen_string_literal: true
#
# スタッフモデルを定義するインフラストラクチャモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Model
    # staffs テーブルに対応する ActiveRecord モデルです。
    class Staff < ActiveRecord::Base
      self.table_name = "staffs"
    end
  end
end
