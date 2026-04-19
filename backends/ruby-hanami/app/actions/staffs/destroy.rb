module Authorization
  module Actions
    module Staffs
      class Destroy < Authorization::Action
        def handle(request, response)
          response.status = 204
        end
      end
    end
  end
end
