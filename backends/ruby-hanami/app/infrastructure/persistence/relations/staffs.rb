# frozen_string_literal: true

module Infrastructure
  module Persistence
    module Relations
      class Staffs < ROM::Relation[:sql]
        schema(:staffs, infer: true)
      end
    end
  end
end
