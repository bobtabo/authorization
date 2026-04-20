require "cgi"

module Authorization
  module Actions
    module Auth
      class GoogleRedirect < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          cfg = container[:cfg]
          url = "https://accounts.google.com/o/oauth2/auth" \
                "?client_id=#{cfg.oauth.google_client_id}" \
                "&redirect_uri=#{CGI.escape(cfg.oauth.google_redirect_url)}" \
                "&response_type=code&scope=email+profile&access_type=online&state=state"
          response.redirect_to url
        end
      end
    end
  end
end
