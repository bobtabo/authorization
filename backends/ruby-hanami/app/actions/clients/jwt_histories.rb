# frozen_string_literal: true
#
# JWT 履歴一覧アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Clients
      # 指定クライアントの JWT 履歴一覧を返すアクションです。
      class JwtHistories < Authorization::Action
        include Authorization::Actions::Base

        def handle(request, response)
          histories = container[:jwt_history_repo].find_by_client_id(request.params[:id].to_i)
          json_response(response, histories.map { |h|
            {
              id:        h[:id],
              member_id: h[:member_id],
              issue_at:  h[:issue_at]&.strftime("%Y-%m-%d %H:%M:%S"),
              jwt:       h[:jwt],
            }
          })
        end
      end
    end
  end
end
