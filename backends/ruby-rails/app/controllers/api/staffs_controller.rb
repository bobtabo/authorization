class Api::StaffsController < Api::BaseController
  def index       = render json: []
  def update_role = render json: {}
  def restore     = render json: {}
  def destroy     = head :no_content
end
