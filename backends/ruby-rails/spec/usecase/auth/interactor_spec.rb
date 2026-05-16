# frozen_string_literal: true

require "rails_helper"

RSpec.describe UseCase::Auth::Interactor do
  let(:make_entity) do
    ->(id = 1) {
      Domain::Staff::Entity.new(
        id: id, name: "Test Staff", email: "staff@example.com",
        provider: 1, provider_id: "google-123", avatar: nil,
        role: Domain::Staff::Role::MEMBER, last_login_at: nil,
        created_at: Time.current, created_by: nil,
        updated_at: Time.current, updated_by: nil,
        deleted_at: nil, deleted_by: nil, version: 1
      )
    }
  end

  let(:stub_repo) do
    double("StaffRepo",
      find_by_id:       nil,
      find_by_provider: nil,
      save:             make_entity.call,
    )
  end

  let(:stub_auth_repo) do
    double("InvitationAuthRepo", find: Domain::Staff::Role::MEMBER, remove: nil)
  end

  describe "#find_user" do
    it "returns Vo when staff found" do
      entity = make_entity.call(5)
      allow(stub_repo).to receive(:find_by_id).with(5).and_return(entity)
      vo = described_class.new(stub_repo, stub_auth_repo).find_user(5)
      expect(vo.id).to eq 5
      expect(vo.name).to eq "Test Staff"
    end

    it "raises when staff not found" do
      allow(stub_repo).to receive(:find_by_id).and_return(nil)
      expect { described_class.new(stub_repo, stub_auth_repo).find_user(99) }.to raise_error(RuntimeError)
    end
  end

  describe "#login" do
    it "creates new staff when provider not found" do
      allow(stub_repo).to receive(:find_by_provider).and_return(nil)
      saved = make_entity.call(10)
      saved.name = "New User"
      saved.role = Domain::Staff::Role::MEMBER
      allow(stub_repo).to receive(:save).and_return(saved)
      dto = UseCase::Auth::LoginDto.new(provider: 1, provider_id: "new-id", name: "New User", email: "new@example.com", avatar: nil, invitation_token: "tok")
      vo = described_class.new(stub_repo, stub_auth_repo).login(dto)
      expect(vo.name).to eq "New User"
      expect(vo.role).to eq Domain::Staff::Role::MEMBER
    end

    it "updates existing staff and returns Vo" do
      existing = make_entity.call(5)
      allow(stub_repo).to receive(:find_by_provider).and_return(existing)
      updated = existing.dup
      updated.avatar = "new-avatar"
      allow(stub_repo).to receive(:save).and_return(updated)
      login_dto = UseCase::Auth::LoginDto.new(provider: 1, provider_id: "google-123", name: "Test Staff", email: "staff@example.com", avatar: "new-avatar")
      vo = described_class.new(stub_repo, stub_auth_repo).login(login_dto)
      expect(vo.id).to eq 5
      expect(vo.avatar).to eq "new-avatar"
    end
  end
end
