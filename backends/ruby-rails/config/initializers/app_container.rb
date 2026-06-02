# Load DDD layers in dependency order (domain → usecase → infrastructure).
# Files are explicitly required because their naming conventions (e.g. usecase/,
# value_objects.rb defining Vo/ListItem) don't match Zeitwerk's expectations.
%w[support domain usecase infrastructure requests].each do |layer|
  Dir[Rails.root.join("app/#{layer}/**/*.rb")].sort.each { |f| require f }
end

require Rails.root.join("app/config/app_config")
require Rails.root.join("app/config/container")
