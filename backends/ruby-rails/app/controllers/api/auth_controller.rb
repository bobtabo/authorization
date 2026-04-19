class Api::AuthController < Api::BaseController
  def google_redirect = head :ok
  def google_callback  = head :ok
  def get_my_profile   = render json: {}
  def login            = render json: {}
  def logout           = render json: {}
  def invitation       = render json: {}
end
