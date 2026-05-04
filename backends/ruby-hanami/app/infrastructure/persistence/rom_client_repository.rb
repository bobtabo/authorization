# frozen_string_literal: true

require "openssl"
require "securerandom"
require "base64"

module Infrastructure
  module Persistence
    class RomClientRepository
      def initialize(rom)
        @ds = rom.gateways[:default].connection[:clients]
      end

      def find_by_condition(cond)
        q = @ds.where(deleted_at: nil)

        if cond.keyword && !cond.keyword.to_s.empty?
          kw = "%#{cond.keyword}%"
          q = q.where(Sequel.|(Sequel.like(:name, kw), Sequel.like(:email, kw)))
        end

        q = q.where(status: cond.statuses) if cond.statuses && !cond.statuses.empty?
        q = q.where { start_at >= cond.start_from } if cond.start_from
        q = q.where { start_at <= cond.start_to }   if cond.start_to

        q.order(Sequel.desc(:created_at)).all.map { |r| row_to_list_item(r) }
      end

      def find_by_id(id)
        r = @ds.where(id: id, deleted_at: nil).first
        raise "client_not_found" unless r
        row_to_entity(r)
      end

      def save(entity)
        if entity.id.nil? || entity.id == 0
          now = Time.now
          id = @ds.insert(
            name:         entity.name,
            identifier:   entity.identifier,
            post_code:    entity.post_code,
            pref:         entity.pref,
            city:         entity.city,
            address:      entity.address,
            building:     entity.building,
            tel:          entity.tel,
            email:        entity.email,
            access_token: entity.access_token,
            private_key:  entity.private_key,
            public_key:   entity.public_key,
            fingerprint:  entity.fingerprint,
            status:       entity.status,
            start_at:     entity.start_at,
            stop_at:      entity.stop_at,
            created_at:   entity.created_at || now,
            created_by:   entity.created_by,
            updated_at:   entity.updated_at || now,
            updated_by:   entity.updated_by,
            version:      entity.version || 1,
          )
          r = @ds.where(id: id).first
        else
          affected = @ds.where(id: entity.id, version: entity.version).update(
            name:       entity.name,
            post_code:  entity.post_code,
            pref:       entity.pref,
            city:       entity.city,
            address:    entity.address,
            building:   entity.building,
            tel:        entity.tel,
            email:      entity.email,
            status:     entity.status,
            start_at:   entity.start_at,
            stop_at:    entity.stop_at,
            updated_at: entity.updated_at,
            updated_by: entity.updated_by,
            version:    Sequel[:version] + 1,
          )
          raise Domain::ConflictError if affected == 0
          r = @ds.where(id: entity.id).first
        end
        row_to_entity(r)
      end

      def soft_delete(id, deleted_by, version)
        now      = Time.now
        affected = @ds.where(id: id, version: version).update(
          deleted_at: now,
          deleted_by: deleted_by,
          updated_at: now,
          updated_by: deleted_by,
          version:    Sequel[:version] + 1,
        )
        raise Domain::ConflictError if affected == 0
        nil
      end

      def find_by_access_token(token)
        r = @ds.where(access_token: token, deleted_at: nil).first
        r ? row_to_entity(r) : nil
      end

      def find_by_identifier(identifier)
        r = @ds.where(identifier: identifier, deleted_at: nil).first
        r ? row_to_entity(r) : nil
      end

      private

      def row_to_entity(r)
        Support::Assign.call(Domain::Client::Entity.new, r)
      end

      def row_to_list_item(r)
        Domain::Client::ListItem.new(
          id:         r[:id],
          name:       r[:name],
          status:     r[:status],
          start_at:   r[:start_at],
          stop_at:    r[:stop_at],
          created_at: r[:created_at],
          updated_at: r[:updated_at],
        )
      end
    end
  end
end
