# frozen_string_literal: true
#
# クライアントユースケースのインターフェースを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Client
    # クライアントに関するユースケースのインターフェースです。
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
        require "openssl"
        require "securerandom"
        require "base64"

        key         = OpenSSL::PKey::RSA.new(4096)
        priv_pem    = key.to_pem
        pub_pem     = key.public_key.to_pem
        fingerprint = "SHA256:#{Base64.strict_encode64(OpenSSL::Digest::SHA256.digest(key.public_key.to_der)).delete('=')}"
        access_token = SecureRandom.hex(32)
        identifier   = SecureRandom.hex(16)
        now          = Time.now

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
        now    = Time.now

        updated           = entity.dup
        updated.name      = dto.name      if dto.name
        updated.post_code = dto.post_code if dto.post_code
        updated.pref      = dto.pref      if dto.pref
        updated.city      = dto.city      if dto.city
        updated.address   = dto.address   if dto.address
        updated.building  = dto.building  if dto.building
        updated.tel       = dto.tel       if dto.tel
        updated.email     = dto.email     if dto.email

        if dto.status && dto.status != entity.status
          updated.status  = dto.status
          updated.start_at = now if dto.status == Domain::Client::Status::ACTIVE && entity.start_at.nil?
        end

        updated.updated_at = now
        updated.updated_by = dto.executor_id
        updated.version    = dto.version if dto.version

        saved = @repo.save(updated)
        entity_to_detail_vo(saved)
      end

      def destroy(dto)
        entity = @repo.find_by_id(dto.id)
        raise Domain::ConflictError if entity.version != dto.version

        now               = Time.now
        entity.status     = Domain::Client::Status::CLOSED
        entity.updated_at = now
        entity.updated_by = dto.executor_id
        saved = @repo.save(entity)
        @repo.soft_delete(dto.id, dto.executor_id, saved.version)
        nil
      end

      def find_by_access_token(token)
        @repo.find_by_access_token(token)
      end

      def find_by_identifier(identifier)
        @repo.find_by_identifier(identifier)
      end

      # identifier でクライアントを検索し QR コードデータを返します。
      # @param dto [UseCase::Client::QrDto]
      # @return [Domain::Client::QrVo]
      def get_qr(dto)
        entity = @repo.find_by_identifier(dto.identifier)
        raise "client_not_found" unless entity

        Domain::Client::QrVo.new(
          identifier:   entity.identifier,
          deeplink_url: "authgateway://clients/#{entity.identifier}/info",
        )
      end

      # identifier でクライアントを検索しスマホアプリ向け情報を返します。
      # @param dto [UseCase::Client::InfoDto]
      # @return [Domain::Client::InfoVo]
      def get_info(dto)
        entity = @repo.find_by_identifier(dto.identifier)
        raise "client_not_found" unless entity

        Domain::Client::InfoVo.new(
          identifier: entity.identifier,
          name:       entity.name,
          status:     entity.status,
        )
      end

      # 利用開始処理を行い、アクセストークンを返します。
      # Active 以外の場合は Active に遷移し start_at が未設定なら now をセット、stop_at をクリアします。
      # @param dto [UseCase::Client::StartDto]
      # @return [Domain::Client::StartVo]
      def start(dto)
        entity = @repo.find_by_identifier(dto.identifier)
        raise "client_not_found" unless entity

        if entity.status != Domain::Client::Status::ACTIVE
          now = Time.now
          updated          = entity.dup
          updated.status   = Domain::Client::Status::ACTIVE
          updated.start_at = now if entity.start_at.nil?
          updated.stop_at  = nil
          updated.updated_at = now
          updated.updated_by = 0
          entity = @repo.save(updated)
        end

        Domain::Client::StartVo.new(access_token: entity.access_token)
      end

      # 利用停止処理を行います。
      # Active の場合のみ Suspended に遷移し stop_at に now をセットします。
      # @param dto [UseCase::Client::StopDto]
      # @return [void]
      def stop(dto)
        entity = @repo.find_by_identifier(dto.identifier)
        raise "client_not_found" unless entity

        if entity.status == Domain::Client::Status::ACTIVE
          now = Time.now
          updated          = entity.dup
          updated.status   = Domain::Client::Status::SUSPENDED
          updated.stop_at  = now
          updated.updated_at = now
          updated.updated_by = 0
          @repo.save(updated)
        end

        nil
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
