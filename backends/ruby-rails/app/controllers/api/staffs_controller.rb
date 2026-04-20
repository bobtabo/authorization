class Api::StaffsController < Api::BaseController
  def index
    keyword = params[:keyword]
    roles   = Array(params[:roles]).flat_map { |r| r.to_s.split(",") }.filter_map(&:to_i)
    staffs  = container[:staff_uc].find_by_condition(
      Domain::Staff::Condition.new(keyword: keyword, roles: roles)
    )
    render json: { items: staffs.map { |s|
      {
        id:         s.id,
        name:       s.name,
        email:      s.email,
        role:       s.role,
        status:     UseCase::Staff::Interactor.status(s),
        created_at: s.created_at.strftime(TIME_FORMAT),
        updated_at: s.updated_at.strftime(TIME_FORMAT),
      }
    }}
  end

  def update_role
    executor_id = staff_id_from_cookie
    container[:staff_uc].update_role(
      UseCase::Staff::UpdateRoleDto.new(
        id: params[:id].to_i, role: params[:role].to_i, executor_id: executor_id
      )
    )
    render json: { id: params[:id].to_i }
  end

  def restore
    container[:staff_uc].restore(params[:id].to_i)
    render json: { id: params[:id].to_i }
  end

  def destroy
    executor_id = staff_id_from_cookie
    container[:staff_uc].destroy(
      UseCase::Staff::DestroyDto.new(id: params[:id].to_i, executor_id: executor_id)
    )
    render json: { id: params[:id].to_i }
  end
end
