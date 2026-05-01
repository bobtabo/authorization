# frozen_string_literal: true
#
# クライアント登録アクションを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    module Clients
      # 新規クライアントを登録するアクションです。
      # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
      class Store < Authorization::Action
        include Authorization::Actions::Base

        # @param request [Hanami::Action::Request] リクエスト
        # @param response [Hanami::Action::Response] レスポンス
        # @return [void]
        def handle(request, response)
          executor_id = staff_id_from_cookie(request)
          p = request.params
          client = transaction do
            c = container[:client_uc].store(
              ::UseCase::Client::StoreDto.new(
                name:        p[:name].to_s,
                post_code:   p[:post_code].to_s,
                pref:        p[:pref].to_s,
                city:        p[:city].to_s,
                address:     p[:address].to_s,
                building:    p[:building].to_s,
                tel:         p[:tel].to_s,
                email:       p[:email].to_s,
                executor_id: executor_id,
              )
            )
            container[:notification_uc].fan_out(
              ::UseCase::Notification::FanOutDto.new(
                title:        "新しいクライアントが登録されました",
                message:      c.name,
                message_type: 1,
                executor_id:  executor_id,
                url:          "/clients/show?id=#{c.id}",
              )
            )
            c
          end
          Thread.new { container[:mailer].send_access_token(client.email, client.name, client.access_token) }

          json_response(response, { id: client.id }, status: 201)
        end
      end
    end
  end
end
