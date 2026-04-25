# frozen_string_literal: true
#
# 通知リポジトリインターフェースを定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Notification
    # 通知リポジトリのインターフェースです。
    module Repository
      # @param staff_id [Integer] スタッフ ID
      # @param cursor [String, nil] ページネーションカーソル
      # @param limit [Integer] 取得件数上限
      # @return [Domain::Notification::Page] 通知ページ
      def list_page(staff_id, cursor, limit)                                     = raise NotImplementedError

      # @param staff_id [Integer] スタッフ ID
      # @return [Array(Integer, Integer)] 未読件数・総件数の配列
      def counts(staff_id)                                                       = raise NotImplementedError

      # @param staff_id [Integer] スタッフ ID
      # @param ids [Array<Integer>] 対象通知 ID の配列
      # @param all [Boolean] 全件対象フラグ
      # @return [Integer] 更新件数
      def bulk_mark_read(staff_id, ids, all)                                     = raise NotImplementedError

      # @param staff_id [Integer] スタッフ ID
      # @param message_type [Integer] メッセージ種別
      # @param title [String] タイトル
      # @param message [String] メッセージ本文
      # @param created_by [Integer] 作成者 ID
      # @param url [String, nil] 関連 URL
      # @return [Domain::Notification::Entity] 保存した通知エンティティ
      def store(staff_id, message_type, title, message, created_by, url: nil)   = raise NotImplementedError

      # @param id [Integer] 通知 ID
      # @param attrs [Hash] 更新する属性
      # @return [void]
      def patch(id, attrs)                                                       = raise NotImplementedError
    end
  end
end
