# frozen_string_literal: true
#
# ゲートユースケースの実装モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "jwt"
require "openssl"
require "securerandom"

module UseCase
  module Gate
    # ゲート認可に関するユースケースの実装クラスです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param client_repo [Domain::Client::Repository] クライアントリポジトリ
      # @param cache [Domain::Gate::CacheRepository] ゲートキャッシュリポジトリ
      # @param cfg [AppConfig] アプリケーション設定
      def initialize(client_repo, cache, cfg)
        @client_repo = client_repo
        @cache       = cache
        @cfg         = cfg
      end

      def issue_token(dto)
        client = @client_repo.find_by_access_token(dto.access_token)
        raise "client_not_found" unless client

        cached = @cache.get_jwt(client.identifier, dto.member_id)
        return Domain::Gate::IssueVo.new(token: cached) if cached && !cached.empty?

        token = issue_jwt(
          member_id:   dto.member_id,
          identifier:  client.identifier,
          private_key: client.private_key,
          fingerprint: client.fingerprint,
          issuer:      @cfg.jwt.issuer,
          ttl:         @cfg.jwt.ttl,
        )
        begin
          @cache.put_jwt(client.identifier, dto.member_id, token, @cfg.jwt.cache_ttl)
        rescue StandardError
          nil
        end
        Domain::Gate::IssueVo.new(token: token)
      end

      def verify(dto)
        client = @client_repo.find_by_identifier(dto.identifier)
        raise "client_not_found" unless client

        claims = verify_jwt(
          identifier: dto.identifier,
          token:      dto.token,
          public_key: client.public_key,
          issuer:     @cfg.jwt.issuer,
        )
        Domain::Gate::VerifyVo.new(claims: claims)
      end

      private

      def issue_jwt(member_id:, identifier:, private_key:, fingerprint:, issuer:, ttl:)
        now     = Time.now.to_i
        payload = {
          iss: issuer,
          sub: member_id.to_s,
          aud: identifier,
          exp: now + ttl,
          iat: now,
          nbf: now,
          jti: SecureRandom.uuid,
        }
        rsa_key = OpenSSL::PKey::RSA.new(private_key)
        JWT.encode(payload, rsa_key, "RS256", { kid: fingerprint })
      end

      def verify_jwt(identifier:, token:, public_key:, issuer:)
        rsa_key = OpenSSL::PKey::RSA.new(public_key)
        decoded, = JWT.decode(
          token, rsa_key, true,
          algorithms: ["RS256"],
          iss: issuer,
          aud: identifier,
          verify_iss: true,
          verify_aud: true,
        )
        decoded
      end
    end
  end
end
