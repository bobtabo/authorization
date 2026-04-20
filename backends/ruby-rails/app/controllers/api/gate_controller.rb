class Api::GateController < Api::BaseController
  def issue
    member = params[:member]
    return render json: { error: "member_required" }, status: :bad_request if member.blank?

    auth         = request.headers["Authorization"].to_s
    access_token = auth.delete_prefix("Bearer ").strip
    token = container[:gate_uc].issue_token(
      UseCase::Gate::IssueDto.new(access_token: access_token, member_id: member)
    )
    render json: { token: token }
  end

  def verify
    token = params[:token]
    return render json: { error: "token_required" }, status: :bad_request if token.blank?

    payload = container[:gate_uc].verify(
      UseCase::Gate::VerifyDto.new(identifier: params[:identifier], token: token)
    )
    render json: payload
  end
end
