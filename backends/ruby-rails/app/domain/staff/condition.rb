module Domain
  module Staff
    Condition = Struct.new(:keyword, :roles, keyword_init: true)
  end
end
