module UseCase
  module Client
    StoreDto = Struct.new(
      :name, :post_code, :pref, :city, :address, :building, :tel, :email, :executor_id,
      keyword_init: true
    )

    UpdateDto = Struct.new(
      :id, :name, :post_code, :pref, :city, :address, :building, :tel, :email, :status, :executor_id,
      keyword_init: true
    )

    ListConditionDto = Struct.new(:keyword, :start_from, :start_to, :statuses, keyword_init: true)
  end
end
