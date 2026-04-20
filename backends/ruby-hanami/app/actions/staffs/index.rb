module Authorization
  module Actions
    module Staffs
      class Index < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          keyword = request.params[:keyword]
          roles   = Array(request.params[:roles]).flat_map { |r| r.to_s.split(",") }.filter_map(&:to_i)
          staffs  = container[:staff_uc].find_by_condition(
            Domain::Staff::Condition.new(keyword: keyword, roles: roles)
          )
          json_response(response, { items: staffs.map { |s|
            {
              id:         s.id,
              name:       s.name,
              email:      s.email,
              role:       s.role,
              status:     UseCase::Staff::Interactor.status(s),
              created_at: s.created_at.strftime(TIME_FORMAT),
              updated_at: s.updated_at.strftime(TIME_FORMAT),
            }
          }})
        end
      end
    end
  end
end
