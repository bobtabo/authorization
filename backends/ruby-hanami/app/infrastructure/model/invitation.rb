module Infrastructure
  module Model
    Invitation = Struct.new(
      :id, :token,
      :created_at, :created_by, :updated_at, :updated_by,
      :deleted_at, :deleted_by, :version,
      keyword_init: true
    )
  end
end
