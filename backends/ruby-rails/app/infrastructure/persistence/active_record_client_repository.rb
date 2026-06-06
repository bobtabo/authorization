# frozen_string_literal: true
#
# クライアントリポジトリの ActiveRecord 実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # ActiveRecord を用いたクライアントリポジトリの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class ActiveRecordClientRepository
      def initialize
        @model = Infrastructure::Model::Client
      end

      def find_by_condition(cond)
        q = apply_filters(cond)
        q = apply_sort(q, cond)
        q = q.offset(cond.offset).limit(cond.limit) if cond.limit && cond.limit > 0
        q.map { |r| row_to_list_item(r) }
      end

      def count_by_condition(cond)
        apply_filters(cond).count
      end

      private

      def apply_filters(cond)
        q = @model.all
        if cond.keyword.present?
          q = q.where("name LIKE ? OR email LIKE ?", "%#{cond.keyword}%", "%#{cond.keyword}%")
        end
        q = q.where(status: cond.statuses) if cond.statuses.present?
        q = q.where("start_at >= ?", cond.start_from) if cond.start_from
        q = q.where("start_at <= ?", cond.start_to)   if cond.start_to
        q
      end

      def apply_sort(q, cond)
        if cond.sort.present?
          dir = cond.sort_type == "desc" ? :desc : :asc
          q.order(cond.sort => dir)
        else
          q.order(created_at: :desc)
        end
      end

      public

      def find_by_id(id)
        r = @model.find_by(id: id, deleted_at: nil)
        raise "client_not_found" unless r
        row_to_entity(r)
      end

      def save(entity)
        if entity.id.nil? || entity.id == 0
          r = @model.create!(
            name:        entity.name,
            identifier:  entity.identifier,
            post_code:   entity.post_code,
            pref:        entity.pref,
            city:        entity.city,
            address:     entity.address,
            building:    entity.building,
            tel:         entity.tel,
            email:       entity.email,
            access_token: entity.access_token,
            private_key: entity.private_key,
            public_key:  entity.public_key,
            fingerprint: entity.fingerprint,
            status:      entity.status,
            start_at:    entity.start_at,
            stop_at:     entity.stop_at,
            created_at:  entity.created_at,
            created_by:  entity.created_by,
            updated_at:  entity.updated_at,
            updated_by:  entity.updated_by,
            version:     entity.version,
          )
          row_to_entity(r)
        else
          r = @model.find(entity.id)
          raise Domain::ConflictError if r.version != entity.version
          r.update!(
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
            version:    entity.version + 1,
          )
          row_to_entity(r.reload)
        end
      end

      def soft_delete(id, deleted_by, version)
        now      = Time.current
        affected = @model.where(id: id, version: version).update_all(
          deleted_at: now,
          deleted_by: deleted_by,
          updated_at: now,
          updated_by: deleted_by,
          version:    Arel.sql("version + 1"),
        )
        raise Domain::ConflictError if affected == 0
        nil
      end

      def find_by_access_token(token)
        r = @model.find_by(access_token: token, deleted_at: nil)
        r ? row_to_entity(r) : nil
      end

      def find_by_identifier(identifier)
        r = @model.find_by(identifier: identifier, deleted_at: nil)
        r ? row_to_entity(r) : nil
      end

      private

      def row_to_entity(r)
        Support::Assign.call(Domain::Client::Entity.new, r)
      end

      def row_to_list_item(r)
        Domain::Client::ListItem.new(
          id:         r.id,
          name:       r.name,
          status:     r.status,
          start_at:   r.start_at,
          stop_at:    r.stop_at,
          created_at: r.created_at,
          updated_at: r.updated_at,
        )
      end
    end
  end
end
