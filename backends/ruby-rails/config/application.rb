require_relative "boot"
require "rails/all"

Bundler.require(*Rails.groups)

module RubyRails
  class Application < Rails::Application
    config.load_defaults 8.1
    config.api_only = true
    config.time_zone = "Tokyo"
    config.i18n.default_locale = :ja

    # Use app/ as the single Zeitwerk root so DDD namespaces (Infrastructure::,
    # UseCase::, Domain::) are resolvable as top-level constants.
    #
    # By default Rails uses glob "{*,*/concerns}" on "app", which makes every
    # subdir (app/infrastructure, app/usecase, …) a separate Zeitwerk root.
    # Removing the glob makes app/ itself the root instead.
    config.middleware.use ActionDispatch::Cookies

    config.paths["app"].glob = nil
    # app/controllers has its own paths entry in Rails; disable it so it doesn't
    # become a second root conflicting with the app/ root above.
    config.paths["app/controllers"]&.skip_eager_load!

    initializer "setup_ddd_collapse", before: :setup_main_autoloader do |app|
      # controllers/ is transparent: files define Api::Foo, not Controllers::Api::Foo
      Rails.autoloaders.main.collapse(app.root.join("app/controllers").to_s)
      # DDD layers and config/ are explicitly required in initializers;
      # ignore them so Zeitwerk doesn't conflict (naming conventions differ)
      %w[domain usecase infrastructure config middleware requests].each do |dir|
        Rails.autoloaders.main.ignore(app.root.join("app/#{dir}").to_s)
      end
    end
  end
end
