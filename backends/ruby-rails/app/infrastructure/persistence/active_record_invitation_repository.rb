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

      def get_current
        r = @model.where(deleted_at: nil).order(created_at: :desc).first
        r ? build_vo(r.token) : nil
      end

      def issue
        token = SecureRandom.hex(16)
        now   = Time.current
        @model.create!(token: token, created_at: now, updated_at: now)
        build_vo(token)
      end

      def find_by_token(token)
        r = @model.find_by(token: token, deleted_at: nil)
        r ? build_vo(r.token) : nil
      end

      private

      def build_vo(token)
        url         = "#{@frontend_url}/register?token=#{token}"
        display_url = url.length > 50 ? "#{url[0, 47]}..." : url
        Domain::Invitation::Vo.new(token: token, url: url, display_url: display_url)
      end
    end
  end
end
