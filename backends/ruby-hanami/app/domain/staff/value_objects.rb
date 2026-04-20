module Domain
  module Staff
    ListItem = Struct.new(:id, :name, :email, :role, :status, :created_at, :updated_at, keyword_init: true)
    Vo       = Struct.new(:id, :name, :avatar, :role, keyword_init: true)
  end
end
