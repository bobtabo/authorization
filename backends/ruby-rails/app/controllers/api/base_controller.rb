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

  # @return [Integer] Cookie から取得したスタッフ ID
  def staff_id_from_cookie = cookies[:staff_id].to_i

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
