# frozen_string_literal: true
#
# 招待モデルを定義するインフラストラクチャモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Model
    # invitations テーブルに対応する ActiveRecord モデルです。
    class Invitation < ActiveRecord::Base
      self.table_name = "invitations"
    end
  end
end
