module Infrastructure
  module Model
    class Invitation < ActiveRecord::Base
      self.table_name = "invitations"
    end
  end
end
