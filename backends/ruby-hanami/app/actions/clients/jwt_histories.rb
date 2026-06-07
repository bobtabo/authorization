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
          limit     = (request.params[:limit]     || 20).to_i
          page      = (request.params[:page]      || 1).to_i
          offset    = limit * (page - 1)
          sort      = request.params[:sort]      || "issue_at"
          sort_type = request.params[:sort_type] || "desc"

          repo  = container[:jwt_history_repo]
          count = repo.count_by_client_id(request.params[:id].to_i)
          histories = repo.find_by_condition(request.params[:id].to_i, offset: offset, limit: limit, sort: sort, sort_type: sort_type)
          data = histories.map { |h|
            {
              id:        h[:id],
              member_id: h[:member_id],
              issue_at:  h[:issue_at]&.strftime("%Y-%m-%d %H:%M:%S"),
              jwt:       h[:jwt],
            }
          }
          pager = build_pager(count, limit, offset, data.size)
          json_response(response, { data: data, pager: pager })
        end
      end
    end
  end
end
