# frozen_string_literal: true
#
# 招待一覧アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Admin
      module Invitations
        # 現在有効な招待情報を返すアクションです。
        # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
        class Index < Authorization::Action
          include Authorization::Actions::Base

          # @param request [Hanami::Action::Request] リクエスト
          # @param response [Hanami::Action::Response] レスポンス
          # @return [void]
          def handle(request, response)
            role = resolve_role(request, response)
            return unless role

            v = container[:invitation_uc].current(role)
            json_response(response, { found: true, url: v.url, display_url: v.display_url, token: v.token })
          end

          private

          def resolve_role(request, response)
            role_str = request.params[:role].to_s
            if role_str.empty?
              role = 2
            else
              begin
                role = Integer(role_str)
              rescue ArgumentError
                json_response(response, { error: "invalid_role" }, status: 400)
                return nil
              end
            end
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
