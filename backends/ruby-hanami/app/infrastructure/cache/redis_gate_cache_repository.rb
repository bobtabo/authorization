module Infrastructure
  module Cache
    class RedisGateCacheRepository
      def initialize(cfg)
        @redis  = RedisClient.new_client(cfg)
        @prefix = cfg.app.cache_prefix
      end

      def get_jwt(identifier, member_id) = raise NotImplementedError
      def put_jwt(identifier, member_id, token, ttl) = raise NotImplementedError
    end
  end
end
