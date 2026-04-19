class Api::ClientsController < Api::BaseController
  def index   = render json: []
  def store   = render json: {}
  def show    = render json: {}
  def update  = render json: {}
  def destroy = head :no_content
end
