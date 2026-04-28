# frozen_string_literal: true
#
# クライアント更新アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Clients
      # 指定IDのクライアント情報を更新するアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Update < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          executor_id = staff_id_from_cookie(request)
          p = request.params
          client = transaction do
            container[:client_uc].update(
              ::UseCase::Client::UpdateDto.new(
                id:          p[:id].to_i,
                name:        p[:name],
                post_code:   p[:post_code],
                pref:        p[:pref],
                city:        p[:city],
                address:     p[:address],
                building:    p[:building],
                tel:         p[:tel],
                email:       p[:email],
                status:      p[:status]&.to_i,
                executor_id: executor_id,
              )
            )
          end
          json_response(response, {
            id:         client.id,
            name:       client.name,
            identifier: client.identifier,
            post_code:  client.post_code,
            pref:       client.pref,
            city:       client.city,
            address:    client.address,
            building:   client.building,
            tel:        client.tel,
            email:      client.email,
            status:     client.status,
            start_at:   client.start_at&.strftime(TIME_FORMAT),
            stop_at:    client.stop_at&.strftime(TIME_FORMAT),
            created_at: client.created_at.strftime(TIME_FORMAT),
            updated_at: client.updated_at.strftime(TIME_FORMAT),
          })
        end
      end
    end
  end
end
