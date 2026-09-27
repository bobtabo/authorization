# frozen_string_literal: true

require "rails_helper"

RSpec.describe UseCase::Staff::Interactor do
  def make_staff_entity(id:, role: Domain::Staff::Role::ADMIN, deleted_at: nil)
    Domain::Staff::Entity.new(
      id: id, name: "Executor", email: "executor@example.com", provider: 1, provider_id: "p1",
      role: role, deleted_at: deleted_at, version: 1,
    )
  end

  let(:stub_repo) do
    double("StaffRepo",
      count_by_condition: 0,
      find_by_condition:  [],
      find_by_id:         make_staff_entity(id: 2),
      update_role:        true,
      soft_delete:        true,
      restore:            true,
    )
  end

  describe "#find_by_condition" do
    it "returns items and count from repo" do
      items = [
        Domain::Staff::ListItem.new(id: 1, name: "S1", email: "s1@example.com", role: 2, status: 1, created_at: Time.current, updated_at: Time.current),
      ]
      allow(stub_repo).to receive(:count_by_condition).and_return(1)
      allow(stub_repo).to receive(:find_by_condition).and_return(items)
      result = described_class.new(stub_repo).find_by_condition(Domain::Staff::Condition.new)
      expect(result[:items].size).to eq 1
      expect(result[:count]).to eq 1
    end
  end

  describe "#update_role" do
    it "calls repo and returns nil" do
      allow(stub_repo).to receive(:update_role).and_return(true)
      result = described_class.new(stub_repo).update_role(
        UseCase::Staff::UpdateRoleDto.new(id: 1, role: Domain::Staff::Role::ADMIN, executor_id: 2)
      )
      expect(result).to be_nil
    end

    it "実行者IDが0の場合はUnauthorizedErrorを発生させる" do
      expect {
        described_class.new(stub_repo).update_role(
          UseCase::Staff::UpdateRoleDto.new(id: 1, role: Domain::Staff::Role::ADMIN, executor_id: 0)
        )
      }.to raise_error(Domain::UnauthorizedError)
    end

    it "実行者がAdmin以外の場合はForbiddenErrorを発生させる" do
      allow(stub_repo).to receive(:find_by_id).with(2).and_return(make_staff_entity(id: 2, role: Domain::Staff::Role::MEMBER))
      expect {
        described_class.new(stub_repo).update_role(
          UseCase::Staff::UpdateRoleDto.new(id: 1, role: Domain::Staff::Role::ADMIN, executor_id: 2)
        )
      }.to raise_error(Domain::ForbiddenError)
    end

    it "実行者が無効化済みAdminの場合はForbiddenErrorを発生させる" do
      allow(stub_repo).to receive(:find_by_id).with(2).and_return(make_staff_entity(id: 2, deleted_at: Time.current))
      expect {
        described_class.new(stub_repo).update_role(
          UseCase::Staff::UpdateRoleDto.new(id: 1, role: Domain::Staff::Role::ADMIN, executor_id: 2)
        )
      }.to raise_error(Domain::ForbiddenError)
    end

    it "実行者が存在しない場合はForbiddenErrorを発生させる" do
      allow(stub_repo).to receive(:find_by_id).with(2).and_return(nil)
      expect {
        described_class.new(stub_repo).update_role(
          UseCase::Staff::UpdateRoleDto.new(id: 1, role: Domain::Staff::Role::ADMIN, executor_id: 2)
        )
      }.to raise_error(Domain::ForbiddenError)
    end
  end

  describe "#restore" do
    it "calls repo and returns nil" do
      allow(stub_repo).to receive(:restore).and_return(true)
      result = described_class.new(stub_repo).restore(1)
      expect(result).to be_nil
    end
  end

  describe "#destroy" do
    it "calls soft_delete and returns nil" do
      allow(stub_repo).to receive(:soft_delete).and_return(true)
      result = described_class.new(stub_repo).destroy(
        UseCase::Staff::DestroyDto.new(id: 1, executor_id: 2, version: 1)
      )
      expect(result).to be_nil
    end
  end
end
