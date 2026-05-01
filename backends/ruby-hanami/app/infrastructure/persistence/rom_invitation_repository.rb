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
        @ds.insert(token: token, created_at: now, created_by: 0, updated_at: now, updated_by: 0)
        build_vo(token)
      end

      def find_by_token(token)
        r = @ds.where(token: token, deleted_at: nil).first
        r ? build_vo(r[:token]) : nil
      end

      private

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
