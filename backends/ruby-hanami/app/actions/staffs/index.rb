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
          keyword = request.params[:keyword]
          roles   = Array(request.params[:roles]).flat_map { |r| r.to_s.split(",") }.filter_map(&:to_i)
          staffs  = container[:staff_uc].find_by_condition(
            ::Domain::Staff::Condition.new(keyword: keyword, roles: roles)
          )
          json_response(response, { items: staffs.map { |s|
            {
              id:         s.id,
              name:       s.name,
              email:      s.email,
              role:       s.role,
              status:     s.status,
              created_at: s.created_at.strftime(TIME_FORMAT),
              updated_at: s.updated_at.strftime(TIME_FORMAT),
            }
          }})
        end
      end
    end
  end
end
