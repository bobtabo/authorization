# frozen_string_literal: true
#
# アクション共通処理モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Authorization
  module Actions
    # アクション共通の処理を提供するモジュールです。
    module Base
      TIME_FORMAT = "%Y-%m-%d %H:%M"

      # @return [AppContainer] DI コンテナのインスタンス
      def container
        AppContainer.instance
      end

      # @param request [Hanami::Action::Request] リクエストオブジェクト
      # @return [Integer] Cookie から取得したスタッフ ID
      def staff_id_from_cookie(request)
        request.cookies["staff_id"].to_i
      end

      # @param response [Hanami::Action::Response] レスポンスオブジェクト
      # @param data [Hash] レスポンスデータ
      # @param status [Integer] HTTP ステータスコード
      # @return [void]
      def json_response(response, data, status: 200)
        response.status = status
        response.format = :json
        response.body   = data.to_json
      end

      # ROM トランザクションを実行します。
      # @return [Object] ブロックの戻り値
      def transaction(&block)
        container[:rom].gateways[:default].connection.transaction(&block)
      end

      DEFAULT_PAGE_COUNT = 5

      def build_pager(count, limit, offset, record_count)
        limit = 20 if limit <= 0
        page_count = [1, (count.to_f / limit).ceil].max
        last_page_offset = (page_count * limit) - limit
        offset = last_page_offset if count > 0 && offset > last_page_offset
        page = (offset.to_f / limit).ceil + 1
        start_page = [1, page - (DEFAULT_PAGE_COUNT - 1)].max
        end_page = [page_count, start_page + (DEFAULT_PAGE_COUNT - 1)].min
        {
          count: count,
          limit: limit,
          next: page_count > page,
          previous: page > 1,
          page: page,
          nextPage: page + 1,
          previousPage: page - 1,
          pageCount: page_count,
          first: page > 1,
          last: page_count > page,
          firstRecordCount: offset + 1,
          lastRecordCount: offset + record_count,
          startPage: start_page,
          endPage: end_page,
        }
      end
    end
  end
end
