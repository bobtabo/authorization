module Authorization
  module Actions
    module Auth
      class Logout < Authorization::Action
        def handle(request, response)
          response.status = 200
        end
      end
    end
  end
end
