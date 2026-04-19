module Authorization
  module Actions
    module Gate
      class Issue < Authorization::Action
        def handle(request, response)
          response.status = 200
        end
      end
    end
  end
end
