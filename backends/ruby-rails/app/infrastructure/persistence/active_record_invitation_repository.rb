# frozen_string_literal: true
#
# 招待リポジトリの ActiveRecord 実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "securerandom"

module Infrastructure
  module Persistence
    # ActiveRecord を用いた招待リポジトリの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class ActiveRecordInvitationRepository
      def initialize(cfg)
        @model        = Infrastructure::Model::Invitation
        @frontend_url = cfg.app.frontend_url
      end

      def get_current_by_role(role)
        r = @model.where(deleted_at: nil, role: role).order(created_at: :desc).first
        r ? build_entity(r) : nil
      end

      def issue(role)
        token = SecureRandom.hex(16)
        now   = Time.current
        r = @model.where(deleted_at: nil, role: role).order(created_at: :desc).first
        if r.nil?
          r = @model.create!(token: token, role: role, created_at: now, created_by: 0, updated_at: now, updated_by: 0)
        else
          r.update!(token: token, updated_at: now)
        end
        build_entity(r)
      end

      def find_by_token(token)
        r = @model.find_by(token: token, deleted_at: nil)
        r ? build_entity(r) : nil
      end

      def entity_to_vo(entity)
        build_vo(entity.token)
      end

      private

      def build_entity(record)
        Domain::Invitation::Entity.new(
          id:    record.id,
          token: record.token,
          role:  record.role,
        )
      end

      def build_vo(token)
        url         = "#{@frontend_url}/invitation/#{token}"
        display_url = build_display_url(url)
        Domain::Invitation::Vo.new(token: token, url: url, display_url: display_url)
      end

      def build_display_url(url)
        seg = "/invitation/"
        idx = url.index(seg)
        if idx
          base   = url[0, idx + seg.length]
          after  = url[idx + seg.length..]
          tok_end = (after.index(/[?#]/) || after.length)
          tok    = after[0, tok_end]
          suffix = after[tok_end..]
          return "#{base}#{tok[0, 6]}...#{tok[-4..]}#{suffix}" if tok.length > 13
        end
        url.length > 72 ? "#{url[0, 68]}..." : url
      end
    end
  end
end
