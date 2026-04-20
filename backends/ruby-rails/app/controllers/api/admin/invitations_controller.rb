class Api::Admin::InvitationsController < Api::BaseController
  def index
    v = container[:invitation_uc].current
    render json: { found: true, url: v.url, display_url: v.display_url, token: v.token }
  end

  def issue
    v = container[:invitation_uc].issue
    render json: { found: true, url: v.url, display_url: v.display_url, token: v.token }
  end
end
