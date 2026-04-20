module Authorization
  module Actions
    module Clients
      class Index < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          clients = container[:client_uc].find_by_condition(
            UseCase::Client::ListConditionDto.new(
              keyword:    request.params[:keyword],
              start_from: request.params[:start_from],
              start_to:   request.params[:start_to],
              statuses:   [],
            )
          )
          json_response(response, clients.map { |c|
            {
              id:         c.id,
              name:       c.name,
              status:     c.status,
              start_at:   c.start_at&.strftime(TIME_FORMAT),
              stop_at:    c.stop_at&.strftime(TIME_FORMAT),
              created_at: c.created_at.strftime(TIME_FORMAT),
              updated_at: c.updated_at.strftime(TIME_FORMAT),
            }
          })
        end
      end
    end
  end
end
