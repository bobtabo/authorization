# frozen_string_literal: true
#
# スタッフ一覧アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Staffs
      # 検索条件でスタッフ一覧を返すアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Index < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          limit  = (request.params[:limit]  || 10).to_i
          page   = (request.params[:page]   || 1).to_i
          offset = limit * (page - 1)
          keyword = request.params[:keyword]
          roles   = Array(request.params[:roles]).flat_map { |r| r.to_s.split(",") }.filter_map(&:to_i)

          result = container[:staff_uc].find_by_condition(
            ::Domain::Staff::Condition.new(
              keyword:   keyword,
              roles:     roles,
              offset:    offset,
              limit:     limit,
              sort:      request.params[:sort],
              sort_type: request.params[:sort_type],
            )
          )

          data = result[:items].map { |s|
            {
              id:         s.id,
              name:       s.name,
              email:      s.email,
              role:       s.role,
              status:     s.status,
              created_at: s.created_at.strftime(TIME_FORMAT),
              updated_at: s.updated_at.strftime(TIME_FORMAT),
            }
          }
          pager = build_pager(result[:count], limit, offset, data.size)
          json_response(response, { data: data, pager: pager })
        end
      end
    end
  end
end
