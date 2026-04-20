module Domain
  module Client
    Condition = Struct.new(:keyword, :start_from, :start_to, :statuses, keyword_init: true)
  end
end
