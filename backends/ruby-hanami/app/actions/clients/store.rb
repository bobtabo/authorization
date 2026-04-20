module Authorization
  module Actions
    module Clients
      class Store < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          executor_id = staff_id_from_cookie(request)
          p = request.params
          client = container[:client_uc].store(
            UseCase::Client::StoreDto.new(
              name:        p[:name].to_s,
              post_code:   p[:post_code].to_s,
              pref:        p[:pref].to_s,
              city:        p[:city].to_s,
              address:     p[:address].to_s,
              building:    p[:building].to_s,
              tel:         p[:tel].to_s,
              email:       p[:email].to_s,
              executor_id: executor_id,
            )
          )

          notif_url = "/clients/show?id=#{client.id}"
          container[:notification_uc].fan_out(
            UseCase::Notification::FanOutDto.new(
              title:        "新しいクライアントが登録されました",
              message:      client.name,
              message_type: 1,
              executor_id:  executor_id,
              url:          notif_url,
            )
          )
          Thread.new { container[:mailer].send_access_token(client.email, client.name, client.access_token) }

          json_response(response, { id: client.id }, status: 201)
        end
      end
    end
  end
end
