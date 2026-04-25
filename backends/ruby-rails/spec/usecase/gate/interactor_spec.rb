# frozen_string_literal: true

require "rails_helper"
require "openssl"

RSpec.describe UseCase::Gate::Interactor do
  let(:rsa_key)    { OpenSSL::PKey::RSA.new(2048) }
  let(:priv_pem)   { rsa_key.to_pem }
  let(:pub_pem)    { rsa_key.public_key.to_pem }
  let(:fingerprint) { "SHA256:test" }

  let(:make_client) do
    ->(id = 1) {
      Domain::Client::Entity.new(
        id: id, name: "C", identifier: "ident-#{id}",
        post_code: nil, pref: nil, city: nil, address: nil, building: nil, tel: nil,
        email: "c@example.com", access_token: "tok-#{id}",
        private_key: priv_pem, public_key: pub_pem, fingerprint: fingerprint,
        status: Domain::Client::Status::ACTIVE,
        start_at: nil, stop_at: nil,
        created_at: Time.current, created_by: nil,
        updated_at: Time.current, updated_by: nil,
        deleted_at: nil, deleted_by: nil, version: 1
      )
    }
  end

  let(:cfg) do
    Config.new(
      app: AppConfig.new(
        env: "test", port: "8080", frontend_url: "http://localhost:3000",
        staff_cookie_lifetime: 60, notification_default_limit: 10, cache_prefix: "test"
      ),
      db: nil, redis: nil, oauth: nil,
      jwt: JwtConfig.new(issuer: "authorization", algorithm: "RS256", ttl: 1800, cache_ttl: 1800),
      mail: nil,
    )
  end

  let(:client_repo) { double("ClientRepo", find_by_access_token: nil, find_by_identifier: nil) }
  let(:cache)       { double("Cache", get_jwt: nil, put_jwt: nil) }

  describe "#issue_token" do
    it "raises when client not found" do
      allow(client_repo).to receive(:find_by_access_token).and_return(nil)
      expect {
        described_class.new(client_repo, cache, cfg).issue_token(
          UseCase::Gate::IssueDto.new(access_token: "bad", member_id: "m1")
        )
      }.to raise_error(RuntimeError)
    end

    it "returns cached token when cache hit" do
      client = make_client.call(1)
      allow(client_repo).to receive(:find_by_access_token).and_return(client)
      allow(cache).to receive(:get_jwt).and_return("cached-token")
      vo = described_class.new(client_repo, cache, cfg).issue_token(
        UseCase::Gate::IssueDto.new(access_token: "tok-1", member_id: "m1")
      )
      expect(vo.token).to eq "cached-token"
    end

    it "issues new JWT when no cache" do
      client = make_client.call(1)
      allow(client_repo).to receive(:find_by_access_token).and_return(client)
      allow(cache).to receive(:get_jwt).and_return(nil)
      allow(cache).to receive(:put_jwt)
      vo = described_class.new(client_repo, cache, cfg).issue_token(
        UseCase::Gate::IssueDto.new(access_token: "tok-1", member_id: "m1")
      )
      expect(vo.token).not_to be_empty
    end
  end

  describe "#verify" do
    it "raises when client not found" do
      allow(client_repo).to receive(:find_by_identifier).and_return(nil)
      expect {
        described_class.new(client_repo, cache, cfg).verify(
          UseCase::Gate::VerifyDto.new(identifier: "bad", token: "tok")
        )
      }.to raise_error(RuntimeError)
    end

    it "verifies a valid token and returns claims" do
      client = make_client.call(1)
      allow(client_repo).to receive(:find_by_access_token).and_return(client)
      allow(client_repo).to receive(:find_by_identifier).and_return(client)
      allow(cache).to receive(:get_jwt).and_return(nil)
      allow(cache).to receive(:put_jwt)

      uc = described_class.new(client_repo, cache, cfg)
      issue_vo = uc.issue_token(UseCase::Gate::IssueDto.new(access_token: "tok-1", member_id: "user42"))
      verify_vo = uc.verify(UseCase::Gate::VerifyDto.new(identifier: "ident-1", token: issue_vo.token))
      expect(verify_vo.claims["sub"]).to eq "user42"
    end
  end
end
