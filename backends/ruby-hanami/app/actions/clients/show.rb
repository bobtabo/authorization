# frozen_string_literal: true
#
# クライアント詳細アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Clients
      # 指定IDのクライアント詳細を返すアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Show < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          c = container[:client_uc].find_by_id(request.params[:id].to_i)
        rescue RuntimeError => e
          response.status = 404
          response.body   = { error: e.message }.to_json
        else
          json_response(response, {
            id:         c.id,
            name:       c.name,
            identifier: c.identifier,
            post_code:  c.post_code,
            pref:       c.pref,
            city:       c.city,
            address:    c.address,
            building:   c.building,
            tel:        c.tel,
            email:      c.email,
            status:     c.status,
            start_at:   c.start_at&.strftime(TIME_FORMAT),
            stop_at:    c.stop_at&.strftime(TIME_FORMAT),
            created_at: c.created_at.strftime(TIME_FORMAT),
            updated_at: c.updated_at.strftime(TIME_FORMAT),
          })
        end
      end
    end
  end
end
