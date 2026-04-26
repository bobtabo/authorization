require "hanami"
require_relative "../app/middleware/json_body_parser"

module Authorization
  class App < Hanami::App
    config.middleware.use Middleware::JsonBodyParser
  end
end
