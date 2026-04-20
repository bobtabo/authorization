module Domain
  module Invitation
    Vo = Struct.new(:token, :url, :display_url, keyword_init: true)
  end
end
