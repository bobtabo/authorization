module UseCase
  module Gate
    class Interactor
      def initialize(client_repo, cache, cfg)
        @client_repo = client_repo
        @cache       = cache
        @cfg         = cfg
      end

      def issue_token(dto) = raise NotImplementedError
      def verify(dto)      = raise NotImplementedError
    end
  end
end
