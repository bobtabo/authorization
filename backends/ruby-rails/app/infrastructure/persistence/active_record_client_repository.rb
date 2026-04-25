# frozen_string_literal: true
#
# クライアントリポジトリの ActiveRecord 実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ActiveRecord を用いたクライアントリポジトリの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class ActiveRecordClientRepository
      def initialize
        @model = Infrastructure::Model::Client
      end
    end
  end
end
