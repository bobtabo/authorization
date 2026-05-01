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
end
