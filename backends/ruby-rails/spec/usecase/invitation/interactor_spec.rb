# frozen_string_literal: true

require "rails_helper"

RSpec.describe UseCase::Invitation::Interactor do
  let(:make_entity) do
    ->(token = "tok123", role = 2) {
      Domain::Invitation::Entity.new(
        id:    1,
        token: token,
        role:  role,
      )
    }
  end

  let(:make_vo) do
    ->(token = "tok123") {
      Domain::Invitation::Vo.new(
        token:       token,
        url:         "http://localhost:3000/invitation/#{token}",
        display_url: "http://localhost:3000/invitation/#{token}",
      )
    }
  end

  let(:stub_repo) do
    double("InvitationRepo",
      get_current_by_role: make_entity.call,
      issue:               make_entity.call("new-token"),
      find_by_token:       nil,
      entity_to_vo:        make_vo.call,
    )
  end

  let(:stub_auth_repo) do
    double("InvitationAuthRepo", store: nil)
  end

  describe "#current" do
    it "returns current invitation from repo" do
      allow(stub_repo).to receive(:entity_to_vo).and_return(make_vo.call("tok123"))
      vo = described_class.new(stub_repo, stub_auth_repo).current(2)
      expect(vo.token).to eq "tok123"
    end

    it "returns nil when no invitation" do
      allow(stub_repo).to receive(:get_current_by_role).and_return(nil)
      expect(described_class.new(stub_repo, stub_auth_repo).current(2)).to be_nil
    end
  end

  describe "#issue" do
    it "returns newly issued invitation" do
      allow(stub_repo).to receive(:entity_to_vo).and_return(make_vo.call("new-token"))
      vo = described_class.new(stub_repo, stub_auth_repo).issue(2)
      expect(vo.token).to eq "new-token"
    end
  end

  describe "#find_by_token" do
    it "returns vo when token found" do
      entity = make_entity.call("abc", 1)
      allow(stub_repo).to receive(:find_by_token).with("abc").and_return(entity)
      allow(stub_repo).to receive(:entity_to_vo).and_return(make_vo.call("abc"))
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
