# frozen_string_literal: true
#
# 通知リポジトリの ActiveRecord 実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ActiveRecord を用いた通知リポジトリの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class ActiveRecordNotificationRepository
      def initialize
        @model = Infrastructure::Model::Notification
      end
    end
  end
end
