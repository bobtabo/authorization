# frozen_string_literal: true

module Infrastructure
  module Persistence
    module Relations
      class Notifications < ROM::Relation[:sql]
        schema(:notifications, infer: true)
      end
    end
  end
end
