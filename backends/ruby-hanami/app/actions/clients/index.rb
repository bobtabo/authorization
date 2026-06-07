# frozen_string_literal: true
#
# クライアント一覧アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Clients
      # 検索条件でクライアント一覧を返すアクションです。
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

          result = container[:client_uc].find_by_condition(
            ::UseCase::Client::ListConditionDto.new(
              keyword:    request.params[:keyword],
              start_from: request.params[:start_from],
              start_to:   request.params[:start_to],
              statuses:   [],
              offset:     offset,
              limit:      limit,
              sort:       request.params[:sort],
              sort_type:  request.params[:sort_type],
            )
          )

          data = result[:items].map { |c|
            {
              id:         c.id,
              name:       c.name,
              status:     c.status,
              start_at:   c.start_at&.strftime(TIME_FORMAT),
              stop_at:    c.stop_at&.strftime(TIME_FORMAT),
              created_at: c.created_at.strftime(TIME_FORMAT),
              updated_at: c.updated_at.strftime(TIME_FORMAT),
            }
          }
          pager = build_pager(result[:count], limit, offset, data.size)
          json_response(response, { data: data, pager: pager })
        end
      end
    end
  end
end
