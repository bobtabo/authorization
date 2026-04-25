# frozen_string_literal: true
#
# ROM を用いた招待リポジトリを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ROM で招待を永続化するリポジトリです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class RomInvitationRepository
      # @param rom [ROM::Container] ROM コンテナ
      def initialize(rom)
        @rom = rom
      end
    end
  end
end
