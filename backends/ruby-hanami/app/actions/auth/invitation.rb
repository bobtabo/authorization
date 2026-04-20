module Authorization
  module Actions
    module Auth
      class Invitation < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          v = container[:invitation_uc].find_by_token(
            UseCase::Invitation::FindByTokenDto.new(token: request.params[:token])
          )
          json_response(response, { found: true, url: v.url, display_url: v.display_url, token: v.token })
        end
      end
    end
  end
end
