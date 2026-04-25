# frozen_string_literal: true
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
    invitation_repo   = Infrastructure::Persistence::RomInvitationRepository.new(rom, cfg)
    notification_repo = Infrastructure::Persistence::RomNotificationRepository.new(rom)
    gate_cache        = Infrastructure::Cache::RedisGateRepository.new(cfg)
    mailer            = Infrastructure::Mail::Mailer.new(cfg)

    {
      cfg:             cfg,
      rom:             rom,
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
