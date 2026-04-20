module Domain
  module Gate
    IssueVo  = Struct.new(:token, keyword_init: true)
    VerifyVo = Struct.new(:claims, keyword_init: true)

    module CacheRepository
      def get_jwt(identifier, member_id)              = raise NotImplementedError
      def put_jwt(identifier, member_id, token, ttl)  = raise NotImplementedError
    end
  end
end
