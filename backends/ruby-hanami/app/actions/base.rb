module Authorization
  module Actions
    module Base
      TIME_FORMAT = "%Y-%m-%d %H:%M"

      def container
        AppContainer.instance
      end

      def staff_id_from_cookie(request)
        request.cookies["staff_id"].to_i
      end

      def json_response(response, data, status: 200)
        response.status = status
        response.format = :json
        response.body   = data.to_json
      end
    end
  end
end
