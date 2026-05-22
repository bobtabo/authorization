# frozen_string_literal: true

module Infrastructure
  module Persistence
    module Relations
      class JwtHistories < ROM::Relation[:sql]
        schema(:jwt_histories, infer: true)
      end
    end
  end
end
