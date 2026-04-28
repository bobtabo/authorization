# frozen_string_literal: true

require "spec_helper"

RSpec.describe UseCase::Invitation::Interactor do
  let(:make_vo) do
    ->(token = "tok123") {
      Domain::Invitation::Vo.new(
        token:       token,
        url:         "http://localhost:3000/register?token=#{token}",
        display_url: "http://localhost:3000/register?token=#{token}",
      )
    }
  end

  let(:stub_repo) do
    double("InvitationRepo",
      get_current:   make_vo.call,
      issue:         make_vo.call("new-token"),
      find_by_token: nil,
    )
  end

  let(:stub_auth_repo) do
    double("InvitationAuthRepo", store: nil)
  end

  describe "#current" do
    it "returns current invitation from repo" do
      vo = described_class.new(stub_repo, stub_auth_repo).current
      expect(vo.token).to eq "tok123"
    end

    it "returns nil when no invitation" do
      allow(stub_repo).to receive(:get_current).and_return(nil)
      expect(described_class.new(stub_repo, stub_auth_repo).current).to be_nil
    end
  end

  describe "#issue" do
    it "returns newly issued invitation" do
      vo = described_class.new(stub_repo, stub_auth_repo).issue
      expect(vo.token).to eq "new-token"
    end
  end

  describe "#find_by_token" do
    it "returns vo when token found" do
      vo = make_vo.call("abc")
      allow(stub_repo).to receive(:find_by_token).with("abc").and_return(vo)
      result = described_class.new(stub_repo, stub_auth_repo).find_by_token(UseCase::Invitation::FindByTokenDto.new(token: "abc"))
      expect(result.token).to eq "abc"
    end

    it "raises when token not found" do
      allow(stub_repo).to receive(:find_by_token).and_return(nil)
      expect {
        described_class.new(stub_repo, stub_auth_repo).find_by_token(UseCase::Invitation::FindByTokenDto.new(token: "bad"))
      }.to raise_error(RuntimeError)
    end
  end
end
