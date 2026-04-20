require "rom"
require "rom-sql"

module Infrastructure
  module Db
    def self.setup(cfg = ConfigLoader.load)
      ROM.container(:sql, cfg.db.dsn) do |c|
        c.gateways[:default].use_logger(Logger.new($stdout))
        c.auto_registration(
          File.join(__dir__, "../persistence/relations"),
          namespace: "Infrastructure::Persistence::Relations",
        )
      end
    end
  end
end
