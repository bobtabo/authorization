# frozen_string_literal: true

require "spec_helper"

RSpec.describe UseCase::Auth::Interactor do
  let(:staff_entity) do
    Domain::Staff::Entity.new(
      id: 1, name: "テスト", email: "t@example.com",
      provider: 1, provider_id: "g123", avatar: "http://img",
      role: Domain::Staff::Role::MEMBER, last_login_at: nil,
      created_at: Time.now, created_by: nil,
      updated_at: Time.now, updated_by: nil,
      deleted_at: nil, deleted_by: nil, version: 1
    )
  end

  let(:stub_repo) do
    double("StaffRepo",
      find_by_id:       staff_entity,
      find_by_provider: nil,
      save:             staff_entity,
    )
  end

  let(:stub_auth_repo) do
    double("InvitationAuthRepo", find: "tok", remove: nil)
  end

  describe "#find_user" do
    it "returns Vo when staff exists" do
      vo = described_class.new(stub_repo, stub_auth_repo).find_user(1)
      expect(vo.id).to eq 1
      expect(vo.name).to eq "テスト"
    end

    it "raises when staff not found" do
      allow(stub_repo).to receive(:find_by_id).and_return(nil)
      expect { described_class.new(stub_repo, stub_auth_repo).find_user(99) }.to raise_error(RuntimeError)
    end
  end

  describe "#login" do
    it "returns Vo for new staff (upsert)" do
      allow(stub_repo).to receive(:find_by_provider).and_return(nil)
      allow(stub_repo).to receive(:save).and_return(staff_entity)
      dto = UseCase::Auth::LoginDto.new(
        provider: 1, provider_id: "g123",
        name: "テスト", email: "t@example.com", avatar: "http://img", invitation_token: "tok"
      )
      vo = described_class.new(stub_repo, stub_auth_repo).login(dto)
      expect(vo.id).to eq 1
    end

    it "returns Vo for existing staff (update)" do
      allow(stub_repo).to receive(:find_by_provider).and_return(staff_entity)
      allow(stub_repo).to receive(:save).and_return(staff_entity)
      dto = UseCase::Auth::LoginDto.new(
        provider: 1, provider_id: "g123",
        name: "テスト", email: "t@example.com", avatar: "http://img"
      )
      vo = described_class.new(stub_repo, stub_auth_repo).login(dto)
      expect(vo.role).to eq Domain::Staff::Role::MEMBER
    end
  end
end
