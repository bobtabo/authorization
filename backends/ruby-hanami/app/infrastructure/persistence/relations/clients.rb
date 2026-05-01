# frozen_string_literal: true

module Infrastructure
  module Persistence
    module Relations
      class Clients < ROM::Relation[:sql]
        schema(:clients, infer: true)
      end
    end
  end
end
