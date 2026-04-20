module Authorization
  module Actions
    module Gate
      class Issue < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          member = request.params[:member]
          if member.nil? || member.to_s.empty?
            return json_response(response, { error: "member_required" }, status: 400)
          end

          auth         = request.env.fetch("HTTP_AUTHORIZATION", "")
          access_token = auth.delete_prefix("Bearer ").strip
          token = container[:gate_uc].issue_token(
            UseCase::Gate::IssueDto.new(access_token: access_token, member_id: member)
          )
          json_response(response, { token: token })
        end
      end
    end
  end
end
