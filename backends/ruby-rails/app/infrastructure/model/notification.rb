module Infrastructure
  module Model
    class Notification < ActiveRecord::Base
      self.table_name = "notifications"
    end
  end
end
