# frozen_string_literal: true
#
# 招待ユースケースの実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Invitation
    # 招待に関するユースケースの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param repo [Domain::Invitation::Repository] 招待リポジトリ
      def initialize(repo)
        @repo = repo
      end

      def current
        @repo.get_current
      end

      def issue
        @repo.issue
      end

      def find_by_token(dto)
        vo = @repo.find_by_token(dto.token)
        raise "invitation_not_found" unless vo
        vo
      end
    end
  end
end
