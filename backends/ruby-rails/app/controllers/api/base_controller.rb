class Api::BaseController < ApplicationController
  TIME_FORMAT = "%Y-%m-%d %H:%M"

  private

  def container        = AppContainer.instance
  def staff_id_from_cookie = cookies[:staff_id].to_i
end
