# frozen_string_literal: true
#
# ゲートトークン発行アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Gate
      # アクセストークンを元にゲートJWTを発行するアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Issue < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          member = request.params[:member]
          if member.nil? || member.to_s.empty?
            return json_response(response, { error: "member_required" }, status: 400)
          end

          auth         = request.env.fetch("HTTP_AUTHORIZATION", "")
          access_token = auth.delete_prefix("Bearer ").strip
          v = container[:gate_uc].issue_token(
            UseCase::Gate::IssueDto.new(access_token: access_token, member_id: member)
          )
          json_response(response, { token: v.token })
        end
      end
    end
  end
end
