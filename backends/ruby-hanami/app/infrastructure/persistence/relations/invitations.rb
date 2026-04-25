# frozen_string_literal: true

module Infrastructure
  module Persistence
    module Relations
      class Invitations < ROM::Relation[:sql]
        schema(:invitations, infer: true)
      end
    end
  end
end
