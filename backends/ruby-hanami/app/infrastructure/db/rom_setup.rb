# frozen_string_literal: true
#
# ROM セットアップを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "rom"
require "rom-sql"

module Infrastructure
  module Db
    # ROM コンテナを初期化します。
    # @param cfg [ConfigLoader] 設定
    # @return [ROM::Container] ROM コンテナ
    def self.setup(cfg = ConfigLoader.load)
      ROM.container(:sql, cfg.db.dsn) do |c|
        c.gateways[:default].use_logger(Logger.new($stdout))
        c.auto_registration(
          File.join(__dir__, "../persistence/relations"),
          namespace: "Infrastructure::Persistence::Relations",
        )
      end
    end
  end
end
