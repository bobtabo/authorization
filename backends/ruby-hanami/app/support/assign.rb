# frozen_string_literal: true
#
# 汎用オブジェクトマッピングモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Support
  module Assign
    def self.call(entity, src, convert: {}, excludes: [])
      entity.members.each do |key|
        next if excludes.include?(key)
        src_key = convert.fetch(key, key)
        value = if src.is_a?(Hash)
                  src[src_key]
                elsif src.respond_to?(src_key)
                  src.public_send(src_key)
                else
                  next
                end
        entity[key] = value
      end
      entity
    end
  end
end
