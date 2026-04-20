module UseCase
  module Staff
    UpdateRoleDto = Struct.new(:id, :role, :executor_id, keyword_init: true)
    DestroyDto    = Struct.new(:id, :executor_id, keyword_init: true)
  end
end
