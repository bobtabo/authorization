module UseCase
  module Invitation
    FindByTokenDto = Struct.new(:token, keyword_init: true)
  end
end
