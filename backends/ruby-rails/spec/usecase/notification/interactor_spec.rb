# frozen_string_literal: true

require "rails_helper"

RSpec.describe UseCase::Notification::Interactor do
  let(:make_entity) do
    ->(id = 1) {
      Domain::Notification::Entity.new(
        id: id, staff_id: 10, message_type: 1,
        title: "Title", message: "Msg", url: nil, read: false,
        created_at: Time.current, created_by: 1,
        updated_at: Time.current, updated_by: 1,
        deleted_at: nil, deleted_by: nil, version: 1
      )
    }
  end

  let(:stub_repo) do
    double("NotificationRepo",
      list_page:      Domain::Notification::Page.new(items: [], next_cursor: nil),
      counts:         Domain::Notification::CountsVo.new(unread: 0, total: 0),
      bulk_mark_read: 0,
      store:          nil,
      patch:          true,
    )
  end

  let(:stub_staff_repo) do
    double("StaffRepo", find_all_active: [])
  end

  describe "#list_page" do
    it "returns page from repo" do
      items = [make_entity.call(1), make_entity.call(2)]
      page  = Domain::Notification::Page.new(items: items, next_cursor: nil)
      allow(stub_repo).to receive(:list_page).and_return(page)
      result = described_class.new(stub_repo, stub_staff_repo).list_page(10, nil, 10)
      expect(result.items.size).to eq 2
    end
  end

  describe "#counts" do
    it "returns CountsVo" do
      allow(stub_repo).to receive(:counts).and_return(Domain::Notification::CountsVo.new(unread: 3, total: 5))
      vo = described_class.new(stub_repo, stub_staff_repo).counts(10)
      expect(vo.unread).to eq 3
      expect(vo.total).to eq 5
    end
  end

  describe "#bulk_mark_read" do
    it "calls repo with all=true and returns nil" do
      allow(stub_repo).to receive(:bulk_mark_read).with(10, [], true).and_return(3)
      result = described_class.new(stub_repo, stub_staff_repo).bulk_mark_read(10)
      expect(result).to be_nil
    end
  end

  describe "#fan_out" do
    it "calls store for each active staff" do
      staffs = [
        Domain::Staff::Entity.new(id: 1, name: "S1", email: "s1@example.com", provider: 1, provider_id: "p1",
          avatar: nil, role: 2, last_login_at: nil, created_at: Time.current, created_by: nil,
          updated_at: Time.current, updated_by: nil, deleted_at: nil, deleted_by: nil, version: 1),
        Domain::Staff::Entity.new(id: 2, name: "S2", email: "s2@example.com", provider: 1, provider_id: "p2",
          avatar: nil, role: 2, last_login_at: nil, created_at: Time.current, created_by: nil,
          updated_at: Time.current, updated_by: nil, deleted_at: nil, deleted_by: nil, version: 1),
      ]
      allow(stub_staff_repo).to receive(:find_all_active).and_return(staffs)
      expect(stub_repo).to receive(:store).exactly(2).times
      dto = UseCase::Notification::FanOutDto.new(
        title: "T", message: "M", message_type: 1, executor_id: 99, url: nil
      )
      described_class.new(stub_repo, stub_staff_repo).fan_out(dto)
    end
  end

  describe "#mark_read" do
    it "calls patch and returns nil" do
      allow(stub_repo).to receive(:patch).with(5, { "read" => true }).and_return(true)
      result = described_class.new(stub_repo, stub_staff_repo).mark_read(5)
      expect(result).to be_nil
    end
  end
end
