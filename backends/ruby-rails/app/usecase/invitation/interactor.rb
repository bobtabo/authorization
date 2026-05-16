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

      def current(role)
        entity = @invitation_repo.get_current_by_role(role)
        entity ? @invitation_repo.entity_to_vo(entity) : nil
      end

      def issue(role)
        entity = @invitation_repo.issue(role)
        @invitation_repo.entity_to_vo(entity)
      end

      def find_by_token(dto)
        entity = @invitation_repo.find_by_token(dto.token)
        raise "invitation_not_found" unless entity
        @invitation_auth_repo.store(entity.token, entity.role, 600)
        @invitation_repo.entity_to_vo(entity)
      end
    end
  end
end
