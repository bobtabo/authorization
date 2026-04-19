module Authorization
  module Actions
    module Staffs
      class Index < Authorization::Action
        def handle(request, response)
          response.status = 200
        end
      end
    end
  end
end
