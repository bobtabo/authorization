# frozen_string_literal: true
#
# スタッフリポジトリの ActiveRecord 実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ActiveRecord を用いたスタッフリポジトリの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class ActiveRecordStaffRepository
      def initialize
        @model = Infrastructure::Model::Staff
      end
    end
  end
end
