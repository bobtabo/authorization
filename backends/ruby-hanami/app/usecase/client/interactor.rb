# frozen_string_literal: true
#
# クライアントユースケースのインターフェースを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Client
    # クライアントに関するユースケースのインターフェースです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param repo [Domain::Client::Repository] クライアントリポジトリ
      def initialize(repo)
        @repo = repo
      end

      # @param dto [UseCase::Client::ListConditionDto] 検索条件 DTO
      # @return [Array<Domain::Client::ListItem>] クライアント一覧
      def find_by_condition(dto) = raise NotImplementedError

      # @param id [Integer] クライアント ID
      # @return [Domain::Client::DetailVo] クライアント詳細VO
      def find_by_id(id)         = raise NotImplementedError

      # @param dto [UseCase::Client::StoreDto] 登録 DTO
      # @return [Domain::Client::StoreResultVo] 登録結果VO
      def store(dto)             = raise NotImplementedError

      # @param dto [UseCase::Client::UpdateDto] 更新 DTO
      # @return [Domain::Client::DetailVo] 更新後の詳細VO
      def update(dto)            = raise NotImplementedError

      # @param id [Integer] クライアント ID
      # @param executor_id [Integer] 実行者 ID
      # @return [void]
      def destroy(id, executor_id) = raise NotImplementedError

      # @param token [String] アクセストークン
      # @return [Domain::Client::Entity] クライアントエンティティ
      def find_by_access_token(token) = raise NotImplementedError

      # @param identifier [String] 識別子
      # @return [Domain::Client::Entity] クライアントエンティティ
      def find_by_identifier(identifier) = raise NotImplementedError
    end
  end
end
