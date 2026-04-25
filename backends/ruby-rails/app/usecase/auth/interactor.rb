# frozen_string_literal: true
#
# 認証ユースケースのインターフェースを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Auth
    # 認証に関するユースケースのインターフェースです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param staff_repo [Domain::Staff::Repository] スタッフリポジトリ
      def initialize(staff_repo)
        @staff_repo = staff_repo
      end

      # @param id [Integer] スタッフ ID
      # @return [Domain::Staff::Vo] スタッフ値オブジェクト
      def find_user(id)         = raise NotImplementedError

      # @param dto [UseCase::Auth::LoginDto] ログイン DTO
      # @return [Domain::Staff::Vo] スタッフ値オブジェクト
      def login(dto)            = raise NotImplementedError
    end
  end
end
