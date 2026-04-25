# frozen_string_literal: true
#
# 招待ユースケースのインターフェースを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Invitation
    # 招待に関するユースケースのインターフェースです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param repo [Domain::Invitation::Repository] 招待リポジトリ
      def initialize(repo)
        @repo = repo
      end

      # @return [Domain::Invitation::Vo] 現在有効な招待VO
      def current               = raise NotImplementedError

      # @return [Domain::Invitation::Vo] 発行した招待VO
      def issue                 = raise NotImplementedError

      # @param dto [UseCase::Invitation::FindByTokenDto] トークン検索 DTO
      # @return [Domain::Invitation::Vo] 招待VO
      def find_by_token(dto)    = raise NotImplementedError
    end
  end
end
