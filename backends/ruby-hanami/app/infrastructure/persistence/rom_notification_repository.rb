# frozen_string_literal: true
#
# ROM を用いた通知リポジトリを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ROM で通知を永続化するリポジトリです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class RomNotificationRepository
      # @param rom [ROM::Container] ROM コンテナ
      def initialize(rom)
        @rom = rom
      end
    end
  end
end
