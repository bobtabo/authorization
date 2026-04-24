module AppContainer
  @mutex = Mutex.new

  def self.instance
    @instance ||= @mutex.synchronize { @instance ||= build }
  end

  def self.build
    cfg = ConfigLoader.load

    client_repo       = Infrastructure::Persistence::ActiveRecordClientRepository.new
    staff_repo        = Infrastructure::Persistence::ActiveRecordStaffRepository.new
    invitation_repo   = Infrastructure::Persistence::ActiveRecordInvitationRepository.new(cfg)
    notification_repo = Infrastructure::Persistence::ActiveRecordNotificationRepository.new
    gate_cache        = Infrastructure::Cache::RedisGateRepository.new(cfg)
    mailer            = Infrastructure::Mail::Mailer.new(cfg.mail)

    {
      cfg:             cfg,
      auth_uc:         UseCase::Auth::Interactor.new(staff_repo),
      client_uc:       UseCase::Client::Interactor.new(client_repo),
      staff_uc:        UseCase::Staff::Interactor.new(staff_repo),
      invitation_uc:   UseCase::Invitation::Interactor.new(invitation_repo),
      gate_uc:         UseCase::Gate::Interactor.new(client_repo, gate_cache, cfg),
      notification_uc: UseCase::Notification::Interactor.new(notification_repo, staff_repo),
      mailer:          mailer,
    }
  end
end
