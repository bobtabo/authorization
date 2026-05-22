# frozen_string_literal: true
# auto_register: false
#
# DI コンテナ定義モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module AppContainer
  @mutex = Mutex.new

  def self.instance
    @instance ||= @mutex.synchronize { @instance ||= build }
  end

  def self.build
    cfg = ConfigLoader.load
    rom = Infrastructure::Db.setup(cfg)

    client_repo       = Infrastructure::Persistence::RomClientRepository.new(rom)
    staff_repo        = Infrastructure::Persistence::RomStaffRepository.new(rom)
    invitation_repo   = Infrastructure::Persistence::RomInvitationRepository.new(rom)
    notification_repo = Infrastructure::Persistence::RomNotificationRepository.new(rom)
    jwt_history_repo      = Infrastructure::Persistence::RomJwtHistoryRepository.new(rom)
    gate_cache            = Infrastructure::Cache::RedisGateRepository.new(cfg)
    invitation_auth_cache = Infrastructure::Cache::RedisInvitationAuthRepository.new(cfg)
    mailer                = Infrastructure::Mail::Mailer.new(cfg)

    {
      cfg:             cfg,
      rom:             rom,
      auth_uc:         UseCase::Auth::Interactor.new(staff_repo, invitation_auth_cache),
      client_uc:       UseCase::Client::Interactor.new(client_repo),
      staff_uc:        UseCase::Staff::Interactor.new(staff_repo),
      invitation_uc:   UseCase::Invitation::Interactor.new(invitation_repo, invitation_auth_cache, cfg.app.frontend_url),
      gate_uc:         UseCase::Gate::Interactor.new(client_repo, gate_cache, cfg, jwt_history_repo),
      notification_uc: UseCase::Notification::Interactor.new(notification_repo, staff_repo),
      jwt_history_repo: jwt_history_repo,
      mailer:          mailer,
    }
  end
end
