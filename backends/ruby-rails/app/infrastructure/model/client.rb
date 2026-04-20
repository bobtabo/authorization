module Infrastructure
  module Model
    class Client < ActiveRecord::Base
      self.table_name = "clients"
    end
  end
end
