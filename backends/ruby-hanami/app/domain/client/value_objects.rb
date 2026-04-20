module Domain
  module Client
    ListItem = Struct.new(:id, :name, :status, :start_at, :stop_at, :created_at, :updated_at, keyword_init: true)

    DetailVo = Struct.new(
      :id, :name, :identifier,
      :post_code, :pref, :city, :address, :building, :tel, :email,
      :status, :start_at, :stop_at, :created_at, :updated_at,
      keyword_init: true
    )
  end
end
