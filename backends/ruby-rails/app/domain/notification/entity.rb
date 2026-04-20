module Domain
  module Notification
    Entity = Struct.new(
      :id, :staff_id, :message_type, :title, :message, :url, :read,
      :created_at, :created_by, :updated_at, :updated_by,
      :deleted_at, :deleted_by, :version,
      keyword_init: true
    )
  end
end
