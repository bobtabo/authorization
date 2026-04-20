module Authorization
  module Actions
    module Clients
      class Update < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          executor_id = staff_id_from_cookie(request)
          p = request.params
          client = container[:client_uc].update(
            UseCase::Client::UpdateDto.new(
              id:          p[:id].to_i,
              name:        p[:name],
              post_code:   p[:post_code],
              pref:        p[:pref],
              city:        p[:city],
              address:     p[:address],
              building:    p[:building],
              tel:         p[:tel],
              email:       p[:email],
              status:      p[:status]&.to_i,
              executor_id: executor_id,
            )
          )
          json_response(response, {
            id:         client.id,
            name:       client.name,
            identifier: client.identifier,
            post_code:  client.post_code,
            pref:       client.pref,
            city:       client.city,
            address:    client.address,
            building:   client.building,
            tel:        client.tel,
            email:      client.email,
            status:     client.status,
            start_at:   client.start_at&.strftime(TIME_FORMAT),
            stop_at:    client.stop_at&.strftime(TIME_FORMAT),
            created_at: client.created_at.strftime(TIME_FORMAT),
            updated_at: client.updated_at.strftime(TIME_FORMAT),
          })
        end
      end
    end
  end
end
