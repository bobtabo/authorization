# frozen_string_literal: true

require "securerandom"

module Infrastructure
  module Persistence
    class RomInvitationRepository
      def initialize(rom, cfg = nil)
        @ds           = rom.gateways[:default].connection[:invitations]
        @frontend_url = cfg&.app&.frontend_url || ENV.fetch("FRONTEND_URL", "http://localhost:3000")
      end

      def get_current
        r = @ds.where(deleted_at: nil).order(Sequel.desc(:created_at)).first
        r ? build_vo(r[:token]) : nil
      end

      def issue
        token = SecureRandom.hex(16)
        now   = Time.now
        @ds.insert(token: token, created_at: now, updated_at: now)
        build_vo(token)
      end

      def find_by_token(token)
        r = @ds.where(token: token, deleted_at: nil).first
        r ? build_vo(r[:token]) : nil
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
