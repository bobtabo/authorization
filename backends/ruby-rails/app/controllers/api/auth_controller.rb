# frozen_string_literal: true
#
# 認証 API コントローラー。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "net/http"
require "json"

# 認証に関する API エンドポイントを提供するコントローラーです。
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
class Api::AuthController < Api::BaseController
  # Google OAuth2 認可画面へリダイレクトします。
  def google_redirect
    cfg = container[:cfg]
    url = "https://accounts.google.com/o/oauth2/auth" \
          "?client_id=#{cfg.oauth.google_client_id}" \
          "&redirect_uri=#{CGI.escape(cfg.oauth.google_redirect_url)}" \
          "&response_type=code&scope=email+profile&access_type=online&state=state"
    redirect_to url, allow_other_host: true
  end

  # Google OAuth2 コールバックを処理します。
  def google_callback
    cfg  = container[:cfg]
    code = params[:code]
    return redirect_to "#{cfg.app.frontend_url}/error?code=500", allow_other_host: true if code.blank?

    access_token = exchange_code_for_token(code, cfg.oauth)
    user_info    = fetch_google_user_info(access_token)

    dto = UseCase::Auth::LoginDto.new(
      provider:    1,
      provider_id: user_info["id"],
      name:        user_info["name"],
      email:       user_info["email"],
      avatar:      user_info["picture"].presence,
    )
    staff = container[:auth_uc].login(dto)

    lifetime = cfg.app.staff_cookie_lifetime * 60
    cookies[:staff_id] = {
      value:     staff.id.to_s,
      max_age:   lifetime,
      path:      "/",
      http_only: true,
      secure:    cfg.app.env == "production",
    }
    redirect_to "#{cfg.app.frontend_url}/clients", allow_other_host: true
  rescue => e
    Rails.logger.error("google_callback error: #{e.message}")
    cfg = container[:cfg]
    redirect_to "#{cfg.app.frontend_url}/error?code=500", allow_other_host: true
  end

  # 認証済みスタッフのプロフィールを返します。
  def get_my_profile
    staff_id = staff_id_from_cookie
    return render json: { error: "unauthenticated" }, status: :unauthorized if staff_id == 0

    s = container[:auth_uc].find_user(staff_id)
    render json: { staff_id: s.id, name: s.name, avatar: s.avatar, role: s.role }
  end

  # ログイン済みスタッフの情報を返します。
  def login
    staff_id = staff_id_from_cookie
    return render json: { error: "unauthenticated" }, status: :unauthorized if staff_id == 0

    s = container[:auth_uc].find_user(staff_id)
    render json: { staff_id: s.id, name: s.name, avatar: s.avatar, role: s.role }
  end

  # ログアウト処理を行います。
  def logout
    render json: {}
  end

  # 招待トークンを検証して招待情報を返します。
  def invitation
    v = container[:invitation_uc].find_by_token(
      UseCase::Invitation::FindByTokenDto.new(token: params[:token])
    )
    render json: { found: true, url: v.url, display_url: v.display_url, token: v.token }
  end

  private

  def exchange_code_for_token(code, oauth_cfg)
    uri  = URI("https://oauth2.googleapis.com/token")
    body = URI.encode_www_form(
      code:          code,
      client_id:     oauth_cfg.google_client_id,
      client_secret: oauth_cfg.google_client_secret,
      redirect_uri:  oauth_cfg.google_redirect_url,
      grant_type:    "authorization_code",
    )
    resp = Net::HTTP.post(uri, body, "Content-Type" => "application/x-www-form-urlencoded")
    JSON.parse(resp.body).fetch("access_token") { raise "token exchange failed: #{resp.body}" }
  end

  def fetch_google_user_info(access_token)
    uri  = URI("https://www.googleapis.com/oauth2/v2/userinfo")
    http = Net::HTTP.new(uri.host, uri.port)
    http.use_ssl = true
    req  = Net::HTTP::Get.new(uri)
    req["Authorization"] = "Bearer #{access_token}"
    JSON.parse(http.request(req).body)
  end
end
