# frozen_string_literal: true
#
# 通知ユースケースのインターフェースを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Notification
    # 通知に関するユースケースのインターフェースです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param repo [Domain::Notification::Repository] 通知リポジトリ
      # @param staff_repo [Domain::Staff::Repository] スタッフリポジトリ
      def initialize(repo, staff_repo)
        @repo       = repo
        @staff_repo = staff_repo
      end

      # @param staff_id [Integer] スタッフ ID
      # @param cursor [String, nil] ページネーションカーソル
      # @param limit [Integer] 取得件数上限
      # @return [Domain::Notification::Page] 通知ページ
      def list_page(staff_id, cursor, limit) = raise NotImplementedError

      # @param staff_id [Integer] スタッフ ID
      # @return [Domain::Notification::CountsVo] 未読件数・総件数VO
      def counts(staff_id)                   = raise NotImplementedError

      # @param staff_id [Integer] スタッフ ID
      # @return [void]
      def bulk_mark_read(staff_id)           = raise NotImplementedError

      # @param dto [UseCase::Notification::FanOutDto] 配信 DTO
      # @return [void]
      def fan_out(dto)                       = raise NotImplementedError

      # @param id [Integer] 通知 ID
      # @return [void]
      def mark_read(id)                      = raise NotImplementedError
    end
  end
end
