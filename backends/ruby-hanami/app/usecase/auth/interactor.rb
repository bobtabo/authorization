# frozen_string_literal: true
#
# 認証ユースケースのインターフェースを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Auth
    # 認証に関するユースケースのインターフェースです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param staff_repo [Domain::Staff::Repository] スタッフリポジトリ
      def initialize(staff_repo)
        @staff_repo = staff_repo
      end

      def find_user(id)
        entity = @staff_repo.find_by_id(id)
        raise "staff_not_found" unless entity
        Domain::Staff::Vo.new(id: entity.id, name: entity.name, avatar: entity.avatar, role: entity.role)
      end

      def login(dto)
        entity = @staff_repo.find_by_provider(dto.provider, dto.provider_id)
        now = Time.now

        if entity
          updated              = entity.dup
          updated.avatar       = dto.avatar
          updated.last_login_at = now
          updated.updated_at   = now
          saved = @staff_repo.save(updated)
        else
          new_entity = Domain::Staff::Entity.new(
            id:            nil,
            name:          dto.name,
            email:         dto.email,
            provider:      dto.provider,
            provider_id:   dto.provider_id,
            avatar:        dto.avatar,
            role:          Domain::Staff::Role::MEMBER,
            last_login_at: now,
            created_at:    now,
            created_by:    nil,
            updated_at:    now,
            updated_by:    nil,
            deleted_at:    nil,
            deleted_by:    nil,
            version:       1,
          )
          saved = @staff_repo.save(new_entity)
        end

        Domain::Staff::Vo.new(id: saved.id, name: saved.name, avatar: saved.avatar, role: saved.role)
      end
    end
  end
end
