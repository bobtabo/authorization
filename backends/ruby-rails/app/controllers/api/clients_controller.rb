class Api::ClientsController < Api::BaseController
  def index
    clients = container[:client_uc].find_by_condition(
      UseCase::Client::ListConditionDto.new(
        keyword:    params[:keyword],
        start_from: params[:start_from],
        start_to:   params[:start_to],
        statuses:   [],
      )
    )
    render json: clients.map { |c|
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
  end

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

  def store
    executor_id = staff_id_from_cookie
    client = container[:client_uc].store(
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

    notif_url = "/clients/show?id=#{client.id}"
    container[:notification_uc].fan_out(
      UseCase::Notification::FanOutDto.new(
        title:        "新しいクライアントが登録されました",
        message:      client.name,
        message_type: 1,
        executor_id:  executor_id,
        url:          notif_url,
      )
    )
    Thread.new { container[:mailer].send_access_token(client.email, client.name, client.access_token) }

    render json: { id: client.id }, status: :created
  end

  def update
    executor_id = staff_id_from_cookie
    client = container[:client_uc].update(
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
      )
    )
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

  def destroy
    executor_id = staff_id_from_cookie
    container[:client_uc].destroy(params[:id].to_i, executor_id)
    render json: {}
  end
end
