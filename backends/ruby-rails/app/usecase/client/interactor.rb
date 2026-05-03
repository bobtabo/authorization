# frozen_string_literal: true
#
# クライアントユースケースの実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "openssl"
require "securerandom"
require "base64"

module UseCase
  module Client
    # クライアントに関するユースケースの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param repo [Domain::Client::Repository] クライアントリポジトリ
      def initialize(repo)
        @repo = repo
      end

      def find_by_condition(dto)
        cond = Domain::Client::Condition.new(
          keyword:    dto.keyword,
          start_from: dto.start_from,
          start_to:   dto.start_to,
          statuses:   dto.statuses,
        )
        @repo.find_by_condition(cond)
      end

      def find_by_id(id)
        entity = @repo.find_by_id(id)
        entity_to_detail_vo(entity)
      end

      def store(dto)
        key         = OpenSSL::PKey::RSA.new(4096)
        priv_pem    = key.to_pem
        pub_pem     = key.public_key.to_pem
        fingerprint = "SHA256:#{Base64.strict_encode64(OpenSSL::Digest::SHA256.digest(key.public_key.to_der)).delete('=')}"
        access_token = SecureRandom.hex(32)
        identifier   = SecureRandom.hex(16)
        now          = Time.current

        entity = Domain::Client::Entity.new(
          id:           nil,
          name:         dto.name,
          identifier:   identifier,
          post_code:    dto.post_code,
          pref:         dto.pref,
          city:         dto.city,
          address:      dto.address,
          building:     dto.building,
          tel:          dto.tel,
          email:        dto.email,
          access_token: access_token,
          private_key:  priv_pem,
          public_key:   pub_pem,
          fingerprint:  fingerprint,
          status:       Domain::Client::Status::INACTIVE,
          start_at:     nil,
          stop_at:      nil,
          created_at:   now,
          created_by:   dto.executor_id,
          updated_at:   now,
          updated_by:   dto.executor_id,
          deleted_at:   nil,
          deleted_by:   nil,
          version:      1,
        )
        saved = @repo.save(entity)
        Domain::Client::StoreResultVo.new(
          id:           saved.id,
          name:         saved.name,
          email:        saved.email,
          access_token: saved.access_token,
        )
      end

      def update(dto)
        entity = @repo.find_by_id(dto.id)
        now    = Time.current

        updated         = entity.dup
        updated.name    = dto.name    if dto.name
        updated.post_code = dto.post_code if dto.post_code
        updated.pref    = dto.pref    if dto.pref
        updated.city    = dto.city    if dto.city
        updated.address = dto.address if dto.address
        updated.building = dto.building if dto.building
        updated.tel     = dto.tel     if dto.tel
        updated.email   = dto.email   if dto.email

        if dto.status && dto.status != entity.status
          updated.status  = dto.status
          updated.start_at = now if dto.status == Domain::Client::Status::ACTIVE && entity.start_at.nil?
        end

        updated.updated_at = now
        updated.updated_by = dto.executor_id

        saved = @repo.save(updated)
        entity_to_detail_vo(saved)
      end

      def destroy(dto)
        @repo.soft_delete(dto.id, dto.executor_id, dto.version)
        nil
      end

      def find_by_access_token(token)
        @repo.find_by_access_token(token)
      end

      def find_by_identifier(identifier)
        @repo.find_by_identifier(identifier)
      end

      private

      def entity_to_detail_vo(e)
        Domain::Client::DetailVo.new(
          id:         e.id,
          name:       e.name,
          identifier: e.identifier,
          post_code:  e.post_code,
          pref:       e.pref,
          city:       e.city,
          address:    e.address,
          building:   e.building,
          tel:        e.tel,
          email:      e.email,
          status:     e.status,
          start_at:   e.start_at,
          stop_at:    e.stop_at,
          created_at: e.created_at,
          updated_at: e.updated_at,
        )
      end
    end
  end
end
