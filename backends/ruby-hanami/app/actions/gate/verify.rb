module Authorization
  module Actions
    module Gate
      class Verify < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          token = request.params[:token]
          if token.nil? || token.to_s.empty?
            return json_response(response, { error: "token_required" }, status: 400)
          end

          payload = container[:gate_uc].verify(
            UseCase::Gate::VerifyDto.new(
              identifier: request.params[:identifier],
              token:      token,
            )
          )
          json_response(response, payload)
        end
      end
    end
  end
end
