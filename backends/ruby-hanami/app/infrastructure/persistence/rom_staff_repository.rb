# frozen_string_literal: true

module Infrastructure
  module Persistence
    class RomStaffRepository
      def initialize(rom)
        @ds = rom.gateways[:default].connection[:staffs]
      end

      def find_by_condition(cond)
        q = @ds

        if cond.keyword && !cond.keyword.to_s.empty?
          kw = "%#{cond.keyword}%"
          q = q.where(Sequel.|(Sequel.like(:name, kw), Sequel.like(:email, kw)))
        end

        q = q.where(role: cond.roles) if cond.roles && !cond.roles.empty?
        q.order(Sequel.desc(:created_at)).all.map { |r| row_to_list_item(r) }
      end

      def find_by_id(id)
        r = @ds.where(id: id).first
        r ? row_to_entity(r) : nil
      end

      def find_by_provider(provider, provider_id)
        r = @ds.where(provider: provider, provider_id: provider_id).first
        r ? row_to_entity(r) : nil
      end

      def find_all_active
        @ds.where(deleted_at: nil).order(Sequel.desc(:created_at)).all.map { |r| row_to_entity(r) }
      end

      def save(entity)
        now = Time.now
        if entity.id.nil? || entity.id == 0
          id = @ds.insert(
            name:          entity.name,
            email:         entity.email,
            provider:      entity.provider,
            provider_id:   entity.provider_id,
            avatar:        entity.avatar,
            role:          entity.role,
            last_login_at: entity.last_login_at,
            created_at:    entity.created_at || now,
            created_by:    entity.created_by,
            updated_at:    entity.updated_at || now,
            updated_by:    entity.updated_by,
            version:       entity.version || 1,
          )
          r = @ds.where(id: id).first
        else
          @ds.where(id: entity.id).update(
            name:          entity.name,
            email:         entity.email,
            avatar:        entity.avatar,
            role:          entity.role,
            last_login_at: entity.last_login_at,
            updated_at:    entity.updated_at,
            updated_by:    entity.updated_by,
            version:       Sequel[:version] + 1,
          )
          r = @ds.where(id: entity.id).first
        end
        row_to_entity(r)
      end

      def update_role(id, role, updated_by)
        @ds.where(id: id).update(role: role, updated_at: Time.now, updated_by: updated_by) > 0
      end

      def soft_delete(id, deleted_by)
        now = Time.now
        @ds.where(id: id).update(
          deleted_at: now, deleted_by: deleted_by,
          updated_at: now, updated_by: deleted_by,
        ) > 0
      end

      def restore(id)
        @ds.where(id: id).update(deleted_at: nil, deleted_by: nil, updated_at: Time.now) > 0
      end

      private

      def row_to_entity(r)
        Domain::Staff::Entity.new(
          id:            r[:id],
          name:          r[:name],
          email:         r[:email],
          provider:      r[:provider],
          provider_id:   r[:provider_id],
          avatar:        r[:avatar],
          role:          r[:role],
          last_login_at: r[:last_login_at],
          created_at:    r[:created_at],
          created_by:    r[:created_by],
          updated_at:    r[:updated_at],
          updated_by:    r[:updated_by],
          deleted_at:    r[:deleted_at],
          deleted_by:    r[:deleted_by],
          version:       r[:version],
        )
      end

      def row_to_list_item(r)
        Domain::Staff::ListItem.new(
          id:         r[:id],
          name:       r[:name],
          email:      r[:email],
          role:       r[:role],
          status:     r[:deleted_at] ? :inactive : :active,
          created_at: r[:created_at],
          updated_at: r[:updated_at],
        )
      end
    end
  end
end
