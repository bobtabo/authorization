# frozen_string_literal: true
#
# LIKE検索用のエスケープ処理モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Infrastructure
  module Persistence
    # LIKE検索用にキーワードをエスケープするモジュールです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    module LikeEscaper
      # LIKE検索用にキーワードをエスケープします。
      #
      # キーワードに含まれる `\`/`%`/`_` はLIKEパターン上でエスケープ文字・ワイルドカードとして
      # 解釈されるため、リテラル文字列として一致させるには事前にエスケープする必要がある。
      # `\` は他の文字のエスケープに使うため最初に変換する。
      #
      # @param keyword [String] エスケープ対象のキーワード
      # @return [String] エスケープ済みのキーワード
      def self.escape(keyword)
        keyword.gsub(/[\\%_]/) { |char| "\\#{char}" }
      end
    end
  end
end
