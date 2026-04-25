# frozen_string_literal: true
#
# ROM を用いたクライアントリポジトリを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ROM でクライアントを永続化するリポジトリです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class RomClientRepository
      # @param rom [ROM::Container] ROM コンテナ
      def initialize(rom)
        @rom = rom
      end
    end
  end
end
