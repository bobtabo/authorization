module Authorization
  module Actions
    module Auth
      class GoogleRedirect < Authorization::Action
        def handle(request, response)
          response.status = 200
        end
      end
    end
  end
end
