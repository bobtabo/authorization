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
      # @param invitation_repo [Domain::Invitation::Repository] 招待リポジトリ
      # @param invitation_auth_repo [Domain::Invitation::AuthRepository] 招待認証キャッシュリポジトリ
      def initialize(invitation_repo, invitation_auth_repo)
        @invitation_repo      = invitation_repo
        @invitation_auth_repo = invitation_auth_repo
      end

      def current
        @invitation_repo.get_current
      end

      def issue
        @invitation_repo.issue
      end

      def find_by_token(dto)
        vo = @invitation_repo.find_by_token(dto.token)
        raise "invitation_not_found" unless vo
        @invitation_auth_repo.store(vo.token, 600)
        vo
      end
    end
  end
end
