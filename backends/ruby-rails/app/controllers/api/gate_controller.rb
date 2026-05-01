# frozen_string_literal: true
#
# ゲート認可 API コントローラー。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

# ゲート認可に関する API エンドポイントを提供するコントローラーです。
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
class Api::GateController < Api::BaseController
  # アクセストークンを検証してゲートトークンを発行します。
  def issue
    member = params[:member]
    return render json: { error: "member_required" }, status: :bad_request if member.blank?

    auth         = request.headers["Authorization"].to_s
    access_token = auth.delete_prefix("Bearer ").strip
    v = container[:gate_uc].issue_token(
      UseCase::Gate::IssueDto.new(access_token: access_token, member_id: member)
    )
    render json: { token: v.token }
  end

  # ゲートトークンを検証してクレームを返します。
  def verify
    token = params[:token]
    return render json: { error: "token_required" }, status: :bad_request if token.blank?

    v = container[:gate_uc].verify(
      UseCase::Gate::VerifyDto.new(identifier: params[:identifier], token: token)
    )
    render json: v.claims
  end
end
