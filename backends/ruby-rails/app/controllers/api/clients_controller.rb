# frozen_string_literal: true
#
# クライアント API コントローラー。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

# クライアントに関する API エンドポイントを提供するコントローラーです。
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
class Api::ClientsController < Api::BaseController
  # クライアント一覧を返します。
  def index
    limit  = (params[:limit]  || 10).to_i
    page   = (params[:page]   || 1).to_i
    offset = limit * (page - 1)

    result = container[:client_uc].find_by_condition(
      UseCase::Client::ListConditionDto.new(
        keyword:    params[:keyword],
        start_from: params[:start_from],
        start_to:   params[:start_to],
        statuses:   [],
        offset:     offset,
        limit:      limit,
        sort:       params[:sort],
        sort_type:  params[:sort_type],
      )
    )

    data = result[:items].map { |c|
      {
        id:         c.id,
        name:       c.name,
        status:     c.status,
        start_at:   c.start_at&.strftime(TIME_FORMAT),
        stop_at:    c.stop_at&.strftime(TIME_FORMAT),
        created_at: c.created_at.strftime(TIME_FORMAT),
        updated_at: c.updated_at.strftime(TIME_FORMAT),
      }
    }
    pager = build_pager(result[:count], limit, offset, data.size)
    render json: { data: data, pager: pager }
  end

  # JWT 履歴一覧を返します。
  def jwt_histories
    limit     = (params[:limit]     || 10).to_i
    page      = (params[:page]      || 1).to_i
    offset    = limit * (page - 1)
    sort      = params[:sort]      || "issue_at"
    sort_type = params[:sort_type] || "desc"

    repo  = container[:jwt_history_repo]
    count = repo.count_by_client_id(params[:id].to_i)
    histories = repo.find_by_condition(params[:id].to_i, offset: offset, limit: limit, sort: sort, sort_type: sort_type)
    data = histories.map { |h|
      {
        id:        h.id,
        member_id: h.member_id,
        issue_at:  h.issue_at&.strftime("%Y-%m-%d %H:%M:%S"),
        jwt:       h.jwt,
      }
    }
    pager = build_pager(count, limit, offset, data.size)
    render json: { data: data, pager: pager }
  end

  # クライアント詳細を返します。
  def show
    c = container[:client_uc].find_by_id(params[:id].to_i)
    render json: {
      id:         c.id,
      name:       c.name,
      identifier: c.identifier,
      post_code:  c.post_code,
      pref:       c.pref,
      city:       c.city,
      address:    c.address,
      building:   c.building,
      tel:        c.tel,
      email:      c.email,
      status:     c.status,
      start_at:   c.start_at&.strftime(TIME_FORMAT),
      stop_at:    c.stop_at&.strftime(TIME_FORMAT),
      created_at: c.created_at.strftime(TIME_FORMAT),
      updated_at: c.updated_at.strftime(TIME_FORMAT),
    }
  end

  # クライアントを登録します。
  def store
    result = Client::StoreClientContract.new.call(params.to_unsafe_h.slice(:name, :post_code, :pref, :city, :address, :building, :tel, :email))
    return render json: { errors: result.errors.to_h }, status: :unprocessable_entity unless result.success?

    executor_id = staff_id_from_cookie
    client = ActiveRecord::Base.transaction do
      c = container[:client_uc].store(
        UseCase::Client::StoreDto.new(
          name:        params[:name].to_s,
          post_code:   params[:post_code].to_s,
          pref:        params[:pref].to_s,
          city:        params[:city].to_s,
          address:     params[:address].to_s,
          building:    params[:building].to_s,
          tel:         params[:tel].to_s,
          email:       params[:email].to_s,
          executor_id: executor_id,
        )
      )
      container[:notification_uc].fan_out(
        UseCase::Notification::FanOutDto.new(
          title:        "新しいクライアントが登録されました",
          message:      c.name,
          message_type: 1,
          executor_id:  executor_id,
          url:          "/clients/show?id=#{c.id}",
        )
      )
      c
    end
    activate_url = "#{container[:cfg].app.frontend_url}/clients/#{client.identifier}/qr"
    Thread.new { container[:mailer].send_activation(client.email, client.name, activate_url) }

    render json: { id: client.id }, status: :created
  end

  # クライアントを更新します。
  def update
    result = Client::UpdateClientContract.new.call(params.to_unsafe_h.slice(:name, :post_code, :pref, :city, :address, :building, :tel, :email, :status, :version))
    return render json: { errors: result.errors.to_h }, status: :unprocessable_entity unless result.success?

    executor_id = staff_id_from_cookie
    client = ActiveRecord::Base.transaction do
      container[:client_uc].update(
        UseCase::Client::UpdateDto.new(
          id:          params[:id].to_i,
          name:        params[:name],
          post_code:   params[:post_code],
          pref:        params[:pref],
          city:        params[:city],
          address:     params[:address],
          building:    params[:building],
          tel:         params[:tel],
          email:       params[:email],
          status:      params[:status]&.to_i,
          executor_id: executor_id,
          version:     params[:version]&.to_i,
        )
      )
    end
    render json: {
      id:         client.id,
      name:       client.name,
      identifier: client.identifier,
      post_code:  client.post_code,
      pref:       client.pref,
      city:       client.city,
      address:    client.address,
      building:   client.building,
      tel:        client.tel,
      email:      client.email,
      status:     client.status,
      start_at:   client.start_at&.strftime(TIME_FORMAT),
      stop_at:    client.stop_at&.strftime(TIME_FORMAT),
      created_at: client.created_at.strftime(TIME_FORMAT),
      updated_at: client.updated_at.strftime(TIME_FORMAT),
    }
  end

  # クライアントを削除します。
  def destroy
    executor_id = staff_id_from_cookie
    ActiveRecord::Base.transaction do
      container[:client_uc].destroy(
        UseCase::Client::DestroyDto.new(id: params[:id].to_i, executor_id: executor_id, version: params[:version].to_i)
      )
    end
    render json: {}
  end

  # QRコードデータを返します。
  def qr
    vo = container[:client_uc].get_qr(
      UseCase::Client::FindByIdentifierDto.new(identifier: params[:identifier])
    )
    return render json: { error: "client_not_found" }, status: :not_found if vo.nil?

    render json: { identifier: vo.identifier, deeplink_url: vo.deeplink_url }
  end

  # クライアント情報（スマホアプリ向け）を返します。
  def info
    vo = container[:client_uc].get_info(
      UseCase::Client::FindByIdentifierDto.new(identifier: params[:identifier])
    )
    return render json: { error: "client_not_found" }, status: :not_found if vo.nil?

    render json: { identifier: vo.identifier, name: vo.name, status: vo.status }
  end

  # 利用開始処理を行い、アクセストークンを返します。
  def start
    vo = ActiveRecord::Base.transaction do
      container[:client_uc].start(
        UseCase::Client::FindByIdentifierDto.new(identifier: params[:identifier])
      )
    end
    return render json: { error: "client_not_found" }, status: :not_found if vo.nil?

    render json: { access_token: vo.access_token }
  end

  # 利用停止処理を行います。
  def stop
    result = ActiveRecord::Base.transaction do
      container[:client_uc].stop(
        UseCase::Client::FindByIdentifierDto.new(identifier: params[:identifier])
      )
    end
    return render json: { error: "client_not_found" }, status: :not_found if result.nil?

    render json: {}
  end
end
