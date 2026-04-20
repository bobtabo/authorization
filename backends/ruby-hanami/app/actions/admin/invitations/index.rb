module Authorization
  module Actions
    module Admin
      module Invitations
        class Index < Authorization::Action
          include Authorization::Actions::Base

          def handle(request, response)
            v = container[:invitation_uc].current
            json_response(response, { found: true, url: v.url, display_url: v.display_url, token: v.token })
          end
        end
      end
    end
  end
end
