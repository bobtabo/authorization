# frozen_string_literal: true
#
# ゲートユースケースのインターフェースを定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Gate
    # ゲート認可に関するユースケースのインターフェースです。
    # @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
    class Interactor
      # @param client_repo [Domain::Client::Repository] クライアントリポジトリ
      # @param cache [Domain::Gate::CacheRepository] ゲートキャッシュリポジトリ
      # @param cfg [AppConfig] アプリケーション設定
      def initialize(client_repo, cache, cfg)
        @client_repo = client_repo
        @cache       = cache
        @cfg         = cfg
      end

      # @param dto [UseCase::Gate::IssueDto] トークン発行 DTO
      # @return [Domain::Gate::IssueVo] 発行結果VO
      def issue_token(dto) = raise NotImplementedError

      # @param dto [UseCase::Gate::VerifyDto] トークン検証 DTO
      # @return [Domain::Gate::VerifyVo] 検証結果VO
      def verify(dto)      = raise NotImplementedError
    end
  end
end
