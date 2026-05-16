# frozen_string_literal: true
#
# 招待ユースケースのインターフェースを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Invitation
    # 招待に関するユースケースのインターフェースです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param invitation_repo [Domain::Invitation::Repository] 招待リポジトリ
      # @param invitation_auth_repo [Domain::Invitation::AuthRepository] 招待認証キャッシュリポジトリ
      # @param frontend_url [String] フロントエンド URL
      def initialize(invitation_repo, invitation_auth_repo, frontend_url = ENV.fetch("FRONTEND_URL", "http://localhost:3000"))
        @invitation_repo      = invitation_repo
        @invitation_auth_repo = invitation_auth_repo
        @frontend_url         = frontend_url
      end

      # @param role [Integer] ロール（1=管理者, 2=メンバー）
      # @return [Domain::Invitation::Vo, nil] 招待値オブジェクト
      def current(role)
        entity = @invitation_repo.get_current_by_role(role)
        entity ? entity_to_vo(entity) : nil
      end

      # @param role [Integer] ロール（1=管理者, 2=メンバー）
      # @return [Domain::Invitation::Vo] 発行した招待値オブジェクト
      def issue(role)
        entity = @invitation_repo.issue(role)
        entity_to_vo(entity)
      end

      # @param dto [UseCase::Invitation::FindByTokenDto] トークン検索 DTO
      # @return [Domain::Invitation::Vo] 招待値オブジェクト
      def find_by_token(dto)
        entity = @invitation_repo.find_by_token(dto.token)
        raise "invitation_not_found" unless entity
        @invitation_auth_repo.store(entity.token, entity.role, 600)
        entity_to_vo(entity)
      end

      private

      def entity_to_vo(entity)
        url         = "#{@frontend_url}/invitation/#{entity.token}"
        display_url = build_display_url(url)
        Domain::Invitation::Vo.new(token: entity.token, url: url, display_url: display_url)
      end

      def build_display_url(url)
        seg = "/invitation/"
        idx = url.index(seg)
        if idx
          base    = url[0, idx + seg.length]
          after   = url[idx + seg.length..]
          tok_end = (after.index(/[?#]/) || after.length)
          tok     = after[0, tok_end]
          suffix  = after[tok_end..]
          return "#{base}#{tok[0, 6]}...#{tok[-4..]}#{suffix}" if tok.length > 13
        end
        url.length > 72 ? "#{url[0, 68]}..." : url
      end
    end
  end
end
