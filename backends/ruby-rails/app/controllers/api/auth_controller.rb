# frozen_string_literal: true
#
# 認証 API コントローラー。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

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
    head :ok
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
end
