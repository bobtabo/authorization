module Authorization
  module Actions
    module Staffs
      class UpdateRole < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          executor_id = staff_id_from_cookie(request)
          id = request.params[:id].to_i
          container[:staff_uc].update_role(
            UseCase::Staff::UpdateRoleDto.new(
              id: id, role: request.params[:role].to_i, executor_id: executor_id
            )
          )
          json_response(response, { id: id })
        end
      end
    end
  end
end
