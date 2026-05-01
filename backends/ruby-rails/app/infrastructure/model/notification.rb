# frozen_string_literal: true
#
# 通知モデルを定義するインフラストラクチャモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Model
    # notifications テーブルに対応する ActiveRecord モデルです。
    class Notification < ActiveRecord::Base
      self.table_name = "notifications"
    end
  end
end
