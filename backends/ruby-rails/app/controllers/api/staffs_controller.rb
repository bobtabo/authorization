# frozen_string_literal: true
#
# スタッフ API コントローラー。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

# スタッフに関する API エンドポイントを提供するコントローラーです。
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
class Api::StaffsController < Api::BaseController
  # スタッフ一覧を返します。
  def index
    limit  = (params[:limit]  || 10).to_i
    page   = (params[:page]   || 1).to_i
    offset = limit * (page - 1)
    keyword = params[:keyword]
    roles   = Array(params[:roles]).flat_map { |r| r.to_s.split(",") }.filter_map(&:to_i)

    result = container[:staff_uc].find_by_condition(
      Domain::Staff::Condition.new(
        keyword:   keyword,
        roles:     roles,
        offset:    offset,
        limit:     limit,
        sort:      params[:sort],
        sort_type: params[:sort_type],
      )
    )

    data = result[:items].map { |s|
      {
        id:         s.id,
        name:       s.name,
        email:      s.email,
        role:       s.role,
        status:     s.status,
        created_at: s.created_at.strftime(TIME_FORMAT),
        updated_at: s.updated_at.strftime(TIME_FORMAT),
      }
    }
    pager = build_pager(result[:count], limit, offset, data.size)
    render json: { data: data, pager: pager }
  end

  # スタッフのロールを更新します。
  def update_role
    executor_id = staff_id_from_cookie
    ActiveRecord::Base.transaction do
      container[:staff_uc].update_role(
        UseCase::Staff::UpdateRoleDto.new(
          id: params[:id].to_i, role: params[:role].to_i, executor_id: executor_id
        )
      )
    end
    render json: { id: params[:id].to_i }
  end

  # 削除済みスタッフを復元します。
  def restore
    ActiveRecord::Base.transaction do
      container[:staff_uc].restore(params[:id].to_i)
    end
    render json: { id: params[:id].to_i }
  end

  # スタッフを削除します。
  def destroy
    executor_id = staff_id_from_cookie
    ActiveRecord::Base.transaction do
      container[:staff_uc].destroy(
        UseCase::Staff::DestroyDto.new(id: params[:id].to_i, executor_id: executor_id, version: params[:version].to_i)
      )
    end
    render json: { id: params[:id].to_i }
  end
end
