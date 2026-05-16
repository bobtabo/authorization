# frozen_string_literal: true

require "securerandom"

module Infrastructure
  module Persistence
    class RomInvitationRepository
      def initialize(rom)
        @ds = rom.gateways[:default].connection[:invitations]
      end

      def get_current_by_role(role)
        r = @ds.where(deleted_at: nil, role: role).order(Sequel.desc(:created_at)).first
        r ? build_entity(r) : nil
      end

      def issue(role)
        token = SecureRandom.hex(16)
        now   = Time.now
        @ds.insert(token: token, role: role, created_at: now, created_by: 0, updated_at: now, updated_by: 0)
        build_entity(@ds.where(token: token).first)
      end

      def find_by_token(token)
        r = @ds.where(token: token, deleted_at: nil).first
        r ? build_entity(r) : nil
      end

      private

      def build_entity(r)
        Domain::Invitation::Entity.new(
          id:    r[:id],
          token: r[:token],
          role:  r[:role],
        )
      end
    end
  end
end
