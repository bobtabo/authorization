# frozen_string_literal: true

require "spec_helper"
require "openssl"

RSpec.describe UseCase::Gate::Interactor do
  let(:rsa_key)   { OpenSSL::PKey::RSA.new(2048) }
  let(:priv_pem)  { rsa_key.to_pem }
  let(:pub_pem)   { rsa_key.public_key.to_pem }
  let(:fingerprint) { "SHA256:test-fp" }

  let(:client_entity) do
    Domain::Client::Entity.new(
      id: 1, name: "CL", identifier: "ident-001",
      post_code: nil, pref: nil, city: nil, address: nil,
      building: nil, tel: nil, email: "c@example.com",
      access_token: "tok-abc", private_key: priv_pem,
      public_key: pub_pem, fingerprint: fingerprint,
      status: 1, start_at: nil, stop_at: nil,
      created_at: Time.now, created_by: nil,
      updated_at: Time.now, updated_by: nil,
      deleted_at: nil, deleted_by: nil, version: 1
    )
  end

  let(:stub_client_repo) do
    double("ClientRepo",
      find_by_access_token: client_entity,
      find_by_identifier:   client_entity,
    )
  end

  let(:stub_cache) do
    double("Cache",
      get_jwt: nil,
      put_jwt: nil,
    )
  end

  let(:stub_cfg) do
    double("cfg",
      jwt: double("jwt_cfg", issuer: "authorization", ttl: 1800, cache_ttl: 1800)
    )
  end

  describe "#issue_token" do
    it "returns IssueVo with a JWT token" do
      vo = described_class.new(stub_client_repo, stub_cache, stub_cfg)
              .issue_token(UseCase::Gate::IssueDto.new(access_token: "tok-abc", member_id: "user-1"))
      expect(vo).to be_a(Domain::Gate::IssueVo)
      expect(vo.token).to be_a(String)
    end

    it "returns cached token when available" do
      allow(stub_cache).to receive(:get_jwt).and_return("cached.jwt.token")
      vo = described_class.new(stub_client_repo, stub_cache, stub_cfg)
              .issue_token(UseCase::Gate::IssueDto.new(access_token: "tok-abc", member_id: "user-1"))
      expect(vo.token).to eq "cached.jwt.token"
    end

    it "raises when client not found" do
      allow(stub_client_repo).to receive(:find_by_access_token).and_return(nil)
      expect {
        described_class.new(stub_client_repo, stub_cache, stub_cfg)
          .issue_token(UseCase::Gate::IssueDto.new(access_token: "bad", member_id: "user-1"))
      }.to raise_error(RuntimeError)
    end
  end

  describe "#verify" do
    it "returns VerifyVo with claims after sign-verify round-trip" do
      uc  = described_class.new(stub_client_repo, stub_cache, stub_cfg)
      iss = uc.issue_token(UseCase::Gate::IssueDto.new(access_token: "tok-abc", member_id: "user-1"))
      vo  = uc.verify(UseCase::Gate::VerifyDto.new(identifier: "ident-001", token: iss.token))
      expect(vo).to be_a(Domain::Gate::VerifyVo)
      expect(vo.claims["sub"]).to eq "user-1"
    end
  end
end
