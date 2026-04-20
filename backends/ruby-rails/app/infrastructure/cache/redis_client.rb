module Infrastructure
  module Cache
    class RedisClient
      def self.new_client(cfg = ConfigLoader.load)
        Redis.new(
          host:     cfg.redis.host,
          port:     cfg.redis.port,
          password: cfg.redis.password.empty? ? nil : cfg.redis.password,
          db:       cfg.redis.db,
        )
      end
    end
  end
end
