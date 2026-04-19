class Api::NotificationsController < Api::BaseController
  def counts   = render json: {}
  def index    = render json: []
  def read_all = head :no_content
  def read     = head :no_content
end
