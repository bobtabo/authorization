module Domain
  module Staff
    module Repository
      def find_by_condition(cond)                   = raise NotImplementedError
      def find_by_id(id)                            = raise NotImplementedError
      def find_by_provider(provider, provider_id)   = raise NotImplementedError
      def find_all_active                            = raise NotImplementedError
      def save(entity)                              = raise NotImplementedError
      def update_role(id, role, updated_by)         = raise NotImplementedError
      def soft_delete(id, deleted_by)               = raise NotImplementedError
      def restore(id)                               = raise NotImplementedError
    end
  end
end
