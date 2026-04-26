# frozen_string_literal: true

module Middleware
  class JsonBodyParser
    def initialize(app)
      @app = app
    end

    def call(env)
      if /json/i.match?(env["CONTENT_TYPE"].to_s)
        raw = env["rack.input"].read
        env["rack.input"].rewind
        unless raw.empty?
          parsed = JSON.parse(raw) rescue {}
          env["rack.request.form_hash"] = (env["rack.request.form_hash"] || {}).merge(parsed)
        end
      end
      @app.call(env)
    end
  end
end
