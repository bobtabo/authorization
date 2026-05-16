# frozen_string_literal: true
#
# 招待発行アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Admin
      module Invitations
        # 招待を新規発行するアクションです。
        # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
        class Issue < Authorization::Action
          include Authorization::Actions::Base

          # @param request [Hanami::Action::Request] リクエスト
          # @param response [Hanami::Action::Response] レスポンス
          # @return [void]
          def handle(request, response)
            staff_id = staff_id_from_cookie(request)
            return json_response(response, { error: "unauthenticated" }, status: 401) if staff_id == 0

            role = resolve_role(request, response)
            return unless role

            v = transaction { container[:invitation_uc].issue(role) }
            json_response(response, { found: true, url: v.url, display_url: v.display_url, token: v.token })
          end

          private

          def resolve_role(request, response)
            role = request.params[:role].to_s
            role = role.empty? ? 2 : role.to_i
            unless [1, 2].include?(role)
              json_response(response, { error: "invalid_role" }, status: 400)
              return nil
            end
            role
          end
        end
      end
    end
  end
end
