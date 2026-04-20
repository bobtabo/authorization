module UseCase
  module Auth
    LoginDto = Struct.new(:provider, :provider_id, :name, :email, :avatar, keyword_init: true)
  end
end
