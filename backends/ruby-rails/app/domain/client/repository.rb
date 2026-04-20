module Domain
  module Client
    module Repository
      def find_by_condition(cond)      = raise NotImplementedError
      def find_by_id(id)               = raise NotImplementedError
      def find_by_access_token(token)  = raise NotImplementedError
      def find_by_identifier(ident)    = raise NotImplementedError
      def save(entity)                 = raise NotImplementedError
      def soft_delete(id, deleted_by)  = raise NotImplementedError
    end
  end
end
