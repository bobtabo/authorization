# frozen_string_literal: true
#
# API 基底コントローラー。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

# API コントローラーの共通処理を提供する基底クラスです。
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
class Api::BaseController < ApplicationController
  TIME_FORMAT = "%Y-%m-%d %H:%M"

  private

  # @return [AppContainer] DI コンテナのインスタンス
  def container            = AppContainer.instance

  # @return [Integer] Cookie から取得した検証済みスタッフ ID
  def staff_id_from_cookie = Support::StaffSession.verify(cookies[:staff_id], container[:cfg].app.staff_cookie_secret)

  # クエリ文字列から配列パラメータを取得します。
  #
  # ブラケット付き（key[]=1&key[]=2）・ブラケット無しの繰り返しキー
  # （key=1&key=2、OpenAPI仕様の style: form, explode: true）のどちらでも
  # 配列として取得できます。Rackの標準的なクエリパース（Rack::Utils.parse_nested_query）は
  # ブラケット無しの繰り返しキーを配列として扱わず最後の値で上書きしてしまうため、
  # 生のクエリ文字列を自前で解析します。
  #
  # @param key [String] パラメータ名
  # @return [Array<String>] 値の一覧（該当パラメータが無ければ空配列）
  def array_query(key)
    query_string = request.query_string
    return [] if query_string.blank?

    query_string.split("&").filter_map do |pair|
      raw_key, raw_value = pair.split("=", 2)
      decoded_key = CGI.unescape(raw_key.to_s)
      CGI.unescape(raw_value.to_s) if decoded_key == key || decoded_key == "#{key}[]"
    end
  end

  DEFAULT_PAGE_COUNT = 5

  def build_pager(count, limit, offset, record_count)
    limit = 10 if limit <= 0
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
