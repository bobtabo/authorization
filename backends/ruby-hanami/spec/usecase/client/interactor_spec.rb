# frozen_string_literal: true

require "spec_helper"

RSpec.describe UseCase::Client::Interactor do
  let(:make_entity) do
    ->(id = 1) {
      Domain::Client::Entity.new(
        id: id, name: "クライアント#{id}", identifier: "id-#{id}",
        post_code: "100-0001", pref: "東京都", city: "千代田区",
        address: "千代田1-1", building: nil, tel: "0312345678",
        email: "c#{id}@example.com", access_token: "token-#{id}",
        private_key: "priv", public_key: "pub", fingerprint: "fp-#{id}",
        status: Domain::Client::Status::INACTIVE,
        start_at: nil, stop_at: nil,
        created_at: Time.now, created_by: 1,
        updated_at: Time.now, updated_by: 1,
        deleted_at: nil, deleted_by: nil, version: 1
      )
    }
  end

  let(:stub_repo) do
    double("ClientRepo",
      count_by_condition:  0,
      find_by_condition:   [],
      find_by_id:          make_entity.call,
      save:                make_entity.call,
      soft_delete:         nil,
      find_by_access_token: nil,
      find_by_identifier:  nil,
    )
  end

  describe "#find_by_condition" do
    it "returns items and count from repo" do
      allow(stub_repo).to receive(:count_by_condition).and_return(1)
      allow(stub_repo).to receive(:find_by_condition).and_return([make_entity.call])
      dto = UseCase::Client::ListConditionDto.new(keyword: nil, start_from: nil, start_to: nil, statuses: nil)
      result = described_class.new(stub_repo).find_by_condition(dto)
      expect(result[:items].size).to eq 1
      expect(result[:count]).to eq 1
    end
  end

  describe "#find_by_id" do
    it "returns DetailVo" do
      vo = described_class.new(stub_repo).find_by_id(1)
      expect(vo.id).to eq 1
      expect(vo).to be_a(Domain::Client::DetailVo)
    end
  end

  describe "#store" do
    it "returns StoreResultVo with access_token" do
      saved = make_entity.call(1)
      allow(stub_repo).to receive(:save).and_return(saved)
      dto = UseCase::Client::StoreDto.new(
        name: "新規", post_code: "100-0001", pref: "東京都",
        city: "千代田区", address: "千代田1-1", building: nil,
        tel: "0312345678", email: "new@example.com", executor_id: 1
      )
      vo = described_class.new(stub_repo).store(dto)
      expect(vo).to be_a(Domain::Client::StoreResultVo)
      expect(vo.access_token).to eq saved.access_token
    end
  end

  describe "#update" do
    it "returns DetailVo" do
      allow(stub_repo).to receive(:save).and_return(make_entity.call)
      dto = UseCase::Client::UpdateDto.new(
        id: 1, name: "更新後", post_code: nil, pref: nil, city: nil,
        address: nil, building: nil, tel: nil, email: nil,
        status: nil, executor_id: 1
      )
      vo = described_class.new(stub_repo).update(dto)
      expect(vo).to be_a(Domain::Client::DetailVo)
    end
  end

  describe "#destroy" do
    it "calls soft_delete and returns nil" do
      allow(stub_repo).to receive(:soft_delete).and_return(nil)
      result = described_class.new(stub_repo).destroy(
        UseCase::Client::DestroyDto.new(id: 1, executor_id: 2, version: 1)
      )
      expect(result).to be_nil
    end
  end

  describe "#find_by_access_token" do
    it "returns entity from repo" do
      entity = make_entity.call
      allow(stub_repo).to receive(:find_by_access_token).with("tok").and_return(entity)
      result = described_class.new(stub_repo).find_by_access_token("tok")
      expect(result.id).to eq 1
    end
  end

  describe "#find_by_identifier" do
    it "returns entity from repo" do
      entity = make_entity.call
      allow(stub_repo).to receive(:find_by_identifier).with("id-1").and_return(entity)
      result = described_class.new(stub_repo).find_by_identifier("id-1")
      expect(result.id).to eq 1
    end
  end

  describe "#get_qr" do
    it "returns QrVo with deeplink_url" do
      entity = make_entity.call
      allow(stub_repo).to receive(:find_by_identifier).with("id-1").and_return(entity)
      vo = described_class.new(stub_repo).get_qr(UseCase::Client::QrDto.new(identifier: "id-1"))
      expect(vo).to be_a(Domain::Client::QrVo)
      expect(vo.identifier).to eq "id-1"
      expect(vo.deeplink_url).to eq "authgateway://clients/id-1/info"
    end

    it "raises when identifier not found" do
      allow(stub_repo).to receive(:find_by_identifier).and_return(nil)
      expect {
        described_class.new(stub_repo).get_qr(UseCase::Client::QrDto.new(identifier: "missing"))
      }.to raise_error(RuntimeError, "client_not_found")
    end
  end

  describe "#get_info" do
    it "returns InfoVo" do
      entity = make_entity.call
      allow(stub_repo).to receive(:find_by_identifier).with("id-1").and_return(entity)
      vo = described_class.new(stub_repo).get_info(UseCase::Client::InfoDto.new(identifier: "id-1"))
      expect(vo).to be_a(Domain::Client::InfoVo)
      expect(vo.identifier).to eq "id-1"
      expect(vo.name).to eq "クライアント1"
      expect(vo.status).to eq Domain::Client::Status::INACTIVE
    end

    it "raises when identifier not found" do
      allow(stub_repo).to receive(:find_by_identifier).and_return(nil)
      expect {
        described_class.new(stub_repo).get_info(UseCase::Client::InfoDto.new(identifier: "missing"))
      }.to raise_error(RuntimeError, "client_not_found")
    end
  end

  describe "#start" do
    it "transitions Inactive to Active and returns StartVo" do
      entity = make_entity.call
      active_entity = entity.dup
      active_entity.status = Domain::Client::Status::ACTIVE
      allow(stub_repo).to receive(:find_by_identifier).and_return(entity)
      allow(stub_repo).to receive(:save).and_return(active_entity)
      vo = described_class.new(stub_repo).start(UseCase::Client::StartDto.new(identifier: "id-1"))
      expect(vo).to be_a(Domain::Client::StartVo)
      expect(vo.access_token).to eq active_entity.access_token
    end

    it "returns StartVo without saving when already Active" do
      entity = make_entity.call(1)
      entity.status = Domain::Client::Status::ACTIVE
      allow(stub_repo).to receive(:find_by_identifier).and_return(entity)
      expect(stub_repo).not_to receive(:save)
      vo = described_class.new(stub_repo).start(UseCase::Client::StartDto.new(identifier: "id-1"))
      expect(vo).to be_a(Domain::Client::StartVo)
    end

    it "raises when identifier not found" do
      allow(stub_repo).to receive(:find_by_identifier).and_return(nil)
      expect {
        described_class.new(stub_repo).start(UseCase::Client::StartDto.new(identifier: "missing"))
      }.to raise_error(RuntimeError, "client_not_found")
    end
  end

  describe "#stop" do
    it "transitions Active to Suspended" do
      entity = make_entity.call
      entity.status = Domain::Client::Status::ACTIVE
      suspended_entity = entity.dup
      suspended_entity.status = Domain::Client::Status::SUSPENDED
      allow(stub_repo).to receive(:find_by_identifier).and_return(entity)
      allow(stub_repo).to receive(:save).and_return(suspended_entity)
      result = described_class.new(stub_repo).stop(UseCase::Client::StopDto.new(identifier: "id-1"))
      expect(result).to be_nil
    end

    it "does nothing when not Active" do
      entity = make_entity.call
      allow(stub_repo).to receive(:find_by_identifier).and_return(entity)
      expect(stub_repo).not_to receive(:save)
      described_class.new(stub_repo).stop(UseCase::Client::StopDto.new(identifier: "id-1"))
    end

    it "raises when identifier not found" do
      allow(stub_repo).to receive(:find_by_identifier).and_return(nil)
      expect {
        described_class.new(stub_repo).stop(UseCase::Client::StopDto.new(identifier: "missing"))
      }.to raise_error(RuntimeError, "client_not_found")
    end
  end
end
