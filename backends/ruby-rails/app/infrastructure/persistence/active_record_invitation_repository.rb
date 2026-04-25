# frozen_string_literal: true
#
# 招待リポジトリの ActiveRecord 実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ActiveRecord を用いた招待リポジトリの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class ActiveRecordInvitationRepository
      def initialize
        @model = Infrastructure::Model::Invitation
      end
    end
  end
end
