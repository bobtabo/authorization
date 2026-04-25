# frozen_string_literal: true
#
# ROM を用いたスタッフリポジトリを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ROM でスタッフを永続化するリポジトリです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class RomStaffRepository
      # @param rom [ROM::Container] ROM コンテナ
      def initialize(rom)
        @rom = rom
      end
    end
  end
end
