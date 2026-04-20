class Api::AuthController < Api::BaseController
  def google_redirect
    cfg = container[:cfg]
    url = "https://accounts.google.com/o/oauth2/auth" \
          "?client_id=#{cfg.oauth.google_client_id}" \
          "&redirect_uri=#{CGI.escape(cfg.oauth.google_redirect_url)}" \
          "&response_type=code&scope=email+profile&access_type=online&state=state"
    redirect_to url, allow_other_host: true
  end

  def google_callback
    head :ok
  end

  def get_my_profile
    staff_id = staff_id_from_cookie
    return render json: { error: "unauthenticated" }, status: :unauthorized if staff_id == 0

    s = container[:auth_uc].find_user(staff_id)
    render json: { staff_id: s.id, name: s.name, avatar: s.avatar, role: s.role }
  end

  def login
    staff_id = staff_id_from_cookie
    return render json: { error: "unauthenticated" }, status: :unauthorized if staff_id == 0

    s = container[:auth_uc].find_user(staff_id)
    render json: { staff_id: s.id, name: s.name, avatar: s.avatar, role: s.role }
  end

  def logout
    render json: {}
  end

  def invitation
    v = container[:invitation_uc].find_by_token(
      UseCase::Invitation::FindByTokenDto.new(token: params[:token])
    )
    render json: { found: true, url: v.url, display_url: v.display_url, token: v.token }
  end
end
