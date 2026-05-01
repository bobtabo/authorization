# frozen_string_literal: true
#
# スタッフリポジトリの ActiveRecord 実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ActiveRecord を用いたスタッフリポジトリの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class ActiveRecordStaffRepository
      def initialize
        @model = Infrastructure::Model::Staff
      end

      def find_by_condition(cond)
        q = @model.all
        if cond.keyword.present?
          q = q.where("name LIKE ? OR email LIKE ?", "%#{cond.keyword}%", "%#{cond.keyword}%")
        end
        q = q.where(role: cond.roles) if cond.roles.present?
        q.order(created_at: :desc).map { |r| row_to_list_item(r) }
      end

      def find_by_id(id)
        @model.find_by(id: id)&.then { |r| row_to_entity(r) }
      end

      def find_by_provider(provider, provider_id)
        r = @model.find_by(provider: provider, provider_id: provider_id)
        r ? row_to_entity(r) : nil
      end

      def find_all_active
        @model.where(deleted_at: nil).order(created_at: :desc).map { |r| row_to_entity(r) }
      end

      def save(entity)
        if entity.id.nil? || entity.id == 0
          r = @model.create!(
            name:          entity.name,
            email:         entity.email,
            provider:      entity.provider,
            provider_id:   entity.provider_id,
            avatar:        entity.avatar,
            role:          entity.role,
            last_login_at: entity.last_login_at,
            created_at:    entity.created_at,
            created_by:    entity.created_by || 0,
            updated_at:    entity.updated_at,
            updated_by:    entity.updated_by || 0,
            version:       entity.version,
          )
          row_to_entity(r)
        else
          r = @model.find(entity.id)
          r.update!(
            name:          entity.name,
            email:         entity.email,
            avatar:        entity.avatar,
            role:          entity.role,
            last_login_at: entity.last_login_at,
            updated_at:    entity.updated_at,
            updated_by:    entity.updated_by || 0,
            version:       entity.version + 1,
          )
          row_to_entity(r.reload)
        end
      end

      def update_role(id, role, updated_by)
        @model.where(id: id).update_all(
          role:       role,
          updated_at: Time.current,
          updated_by: updated_by,
        ) > 0
      end

      def soft_delete(id, deleted_by)
        now = Time.current
        @model.where(id: id).update_all(
          deleted_at: now,
          deleted_by: deleted_by,
          updated_at: now,
          updated_by: deleted_by,
        ) > 0
      end

      def restore(id)
        @model.where(id: id).update_all(
          deleted_at: nil,
          deleted_by: nil,
          updated_at: Time.current,
        ) > 0
      end

      private

      def row_to_entity(r)
        Domain::Staff::Entity.new(
          id:            r.id,
          name:          r.name,
          email:         r.email,
          provider:      r.provider,
          provider_id:   r.provider_id,
          avatar:        r.avatar,
          role:          r.role,
          last_login_at: r.last_login_at,
          created_at:    r.created_at,
          created_by:    r.created_by,
          updated_at:    r.updated_at,
          updated_by:    r.updated_by,
          deleted_at:    r.deleted_at,
          deleted_by:    r.deleted_by,
          version:       r.version,
        )
      end

      def row_to_list_item(r)
        Domain::Staff::ListItem.new(
          id:         r.id,
          name:       r.name,
          email:      r.email,
          role:       r.role,
          status:     r.deleted_at ? :inactive : :active,
          created_at: r.created_at,
          updated_at: r.updated_at,
        )
      end
    end
  end
end
