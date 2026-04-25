# frozen_string_literal: true

require "rails_helper"

RSpec.describe UseCase::Client::Interactor do
  let(:make_entity) do
    ->(id = 1) {
      Domain::Client::Entity.new(
        id: id, name: "Test Client", identifier: "abc123",
        post_code: nil, pref: nil, city: nil, address: nil, building: nil, tel: nil,
        email: "client@example.com", access_token: "token123",
        private_key: "priv", public_key: "pub", fingerprint: "SHA256:xxx",
        status: Domain::Client::Status::INACTIVE,
        start_at: nil, stop_at: nil,
        created_at: Time.current, created_by: 1,
        updated_at: Time.current, updated_by: 1,
        deleted_at: nil, deleted_by: nil, version: 1
      )
    }
  end

  let(:stub_repo) do
    double("ClientRepo",
      find_by_condition:   [],
      find_by_id:          make_entity.call,
      save:                make_entity.call,
      soft_delete:         nil,
      find_by_access_token: nil,
      find_by_identifier:  nil,
    )
  end

  describe "#find_by_condition" do
    it "returns list items from repo" do
      items = [
        Domain::Client::ListItem.new(id: 1, name: "C1", status: 1, start_at: nil, stop_at: nil, created_at: Time.current, updated_at: Time.current),
        Domain::Client::ListItem.new(id: 2, name: "C2", status: 1, start_at: nil, stop_at: nil, created_at: Time.current, updated_at: Time.current),
      ]
      allow(stub_repo).to receive(:find_by_condition).and_return(items)
      dto    = UseCase::Client::ListConditionDto.new
      result = described_class.new(stub_repo).find_by_condition(dto)
      expect(result.size).to eq 2
    end
  end

  describe "#find_by_id" do
    it "returns DetailVo when found" do
      entity = make_entity.call(5)
      allow(stub_repo).to receive(:find_by_id).with(5).and_return(entity)
      vo = described_class.new(stub_repo).find_by_id(5)
      expect(vo.id).to eq 5
      expect(vo.name).to eq "Test Client"
    end
  end

  describe "#store" do
    it "returns StoreResultVo with id and access_token" do
      saved = make_entity.call(10)
      allow(stub_repo).to receive(:save).and_return(saved)
      dto    = UseCase::Client::StoreDto.new(name: "New", email: "n@example.com", executor_id: 1)
      result = described_class.new(stub_repo).store(dto)
      expect(result.id).to eq 10
      expect(result.access_token).to eq "token123"
    end
  end

  describe "#update" do
    it "applies name change and returns DetailVo" do
      original = make_entity.call(3)
      updated  = original.dup
      updated.name = "Updated"
      allow(stub_repo).to receive(:find_by_id).with(3).and_return(original)
      allow(stub_repo).to receive(:save).and_return(updated)
      vo = described_class.new(stub_repo).update(
        UseCase::Client::UpdateDto.new(id: 3, name: "Updated", executor_id: 1)
      )
      expect(vo.name).to eq "Updated"
      expect(vo.id).to eq 3
    end

    it "sets start_at when status changes to ACTIVE" do
      original = make_entity.call(3)
      saved_entity = nil
      repo = double("ClientRepo", find_by_id: original)
      allow(repo).to receive(:save) do |e|
        saved_entity = e
        e
      end
      described_class.new(repo).update(
        UseCase::Client::UpdateDto.new(id: 3, status: Domain::Client::Status::ACTIVE, executor_id: 1)
      )
      expect(saved_entity.start_at).not_to be_nil
    end
  end

  describe "#destroy" do
    it "raises when client not found" do
      allow(stub_repo).to receive(:find_by_id).and_raise("client_not_found")
      expect { described_class.new(stub_repo).destroy(99, 1) }.to raise_error(RuntimeError)
    end
  end

  describe "#find_by_access_token" do
    it "returns nil when not found" do
      allow(stub_repo).to receive(:find_by_access_token).and_return(nil)
      expect(described_class.new(stub_repo).find_by_access_token("bad")).to be_nil
    end
  end

  describe "#find_by_identifier" do
    it "returns entity when found" do
      entity = make_entity.call(7)
      allow(stub_repo).to receive(:find_by_identifier).and_return(entity)
      expect(described_class.new(stub_repo).find_by_identifier("abc123")).not_to be_nil
    end
  end
end
