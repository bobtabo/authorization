# frozen_string_literal: true
#
# アクション共通処理モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "securerandom"

module Authorization
  module Actions
    # アクション共通の処理を提供するモジュールです。
    module Base
      TIME_FORMAT = "%Y-%m-%d %H:%M"

      # OAuth 認可開始時に発行する nonce を保持するクッキー名
      OAUTH_STATE_COOKIE = "oauth_state"
      # nonce クッキーの有効期間（秒）
      OAUTH_STATE_COOKIE_MAX_AGE = 600

      # @return [AppContainer] DI コンテナのインスタンス
      def container
        AppContainer.instance
      end

      # @param request [Hanami::Action::Request] リクエストオブジェクト
      # @return [Integer] Cookie から取得した検証済みスタッフ ID
      def staff_id_from_cookie(request)
        Support::StaffSession.verify(request.cookies["staff_id"], container[:cfg].app.staff_cookie_secret)
      end

      # クエリ文字列から配列パラメータを取得します。
      #
      # ブラケット付き（key[]=1&key[]=2）・ブラケット無しの繰り返しキー
      # （key=1&key=2、OpenAPI仕様の style: form, explode: true）のどちらでも
      # 配列として取得できます。Rackの標準的なクエリパース（Rack::Utils.parse_nested_query）は
      # ブラケット無しの繰り返しキーを配列として扱わず最後の値で上書きしてしまうため、
      # 生のクエリ文字列を自前で解析します。
      #
      # @param request [Hanami::Action::Request] リクエストオブジェクト
      # @param key [String] パラメータ名
      # @return [Array<String>] 値の一覧（該当パラメータが無ければ空配列）
      def array_query(request, key)
        query_string = request.env["QUERY_STRING"].to_s
        return [] if query_string.empty?

        query_string.split("&").filter_map do |pair|
          raw_key, raw_value = pair.split("=", 2)
          decoded_key = CGI.unescape(raw_key.to_s)
          CGI.unescape(raw_value.to_s) if decoded_key == key || decoded_key == "#{key}[]"
        end
      end

      # @param response [Hanami::Action::Response] レスポンスオブジェクト
      # @param data [Hash] レスポンスデータ
      # @param status [Integer] HTTP ステータスコード
      # @return [void]
      def json_response(response, data, status: 200)
        response.status = status
        response.format = :json
        response.body   = data.to_json
      end

      # nonce を生成してクッキーに保存し、state（"{runtime}|{nonce}" または "{runtime}|{nonce}|{token}"）を返します。
      #
      # @param request [Hanami::Action::Request] リクエストオブジェクト
      # @param response [Hanami::Action::Response] レスポンスオブジェクト
      # @param cfg [AppConfig] アプリ設定
      # @return [String] state パラメータ
      def issue_oauth_state(request, response, cfg)
        nonce       = SecureRandom.hex(16)
        secure_flag = cfg.app.env == "production" ? "; Secure" : ""
        append_set_cookie(
          response,
          "#{OAUTH_STATE_COOKIE}=#{nonce}; Path=/; HttpOnly; Max-Age=#{OAUTH_STATE_COOKIE_MAX_AGE}#{secure_flag}; SameSite=Lax",
        )
        token = request.params[:token].to_s
        token.empty? ? "#{cfg.app.runtime}|#{nonce}" : "#{cfg.app.runtime}|#{nonce}|#{token}"
      end

      # state の nonce をクッキーと照合してクッキーを破棄し、[招待トークン, 照合結果] を返します。
      #
      # @param request [Hanami::Action::Request] リクエストオブジェクト
      # @param response [Hanami::Action::Response] レスポンスオブジェクト
      # @return [Array(String, Boolean)] 招待トークン（無ければ nil）と照合結果
      def consume_oauth_state(request, response)
        saved = request.cookies[OAUTH_STATE_COOKIE].to_s
        append_set_cookie(response, "#{OAUTH_STATE_COOKIE}=; Path=/; HttpOnly; Max-Age=0; SameSite=Lax")

        parts = request.params[:state].to_s.split("|", 3)
        nonce = parts[1].to_s
        return [nil, false] if saved.empty? || nonce.empty?
        return [nil, false] unless Rack::Utils.secure_compare(saved, nonce)

        invitation_token = parts[2].to_s
        [invitation_token.empty? ? nil : invitation_token, true]
      end

      # Set-Cookie ヘッダーを追記します（Rack 3 の配列ヘッダー対応）。
      #
      # @param response [Hanami::Action::Response] レスポンスオブジェクト
      # @param cookie [String] Set-Cookie 値
      # @return [void]
      def append_set_cookie(response, cookie)
        current = response.headers["Set-Cookie"]
        response.headers["Set-Cookie"] = current.nil? ? cookie : Array(current) + [cookie]
      end

      # ROM トランザクションを実行します。
      # @return [Object] ブロックの戻り値
      def transaction(&block)
        container[:rom].gateways[:default].connection.transaction(&block)
      end

      DEFAULT_PAGE_COUNT = 5

      def build_pager(count, limit, offset, record_count)
        limit = 10 if limit <= 0
        page_count = [1, (count.to_f / limit).ceil].max
        last_page_offset = (page_count * limit) - limit
        offset = last_page_offset if count > 0 && offset > last_page_offset
        page = (offset.to_f / limit).ceil + 1
        start_page = [1, page - (DEFAULT_PAGE_COUNT - 1)].max
        end_page = [page_count, start_page + (DEFAULT_PAGE_COUNT - 1)].min
        {
          count: count,
          limit: limit,
          next: page_count > page,
          previous: page > 1,
          page: page,
          nextPage: page + 1,
          previousPage: page - 1,
          pageCount: page_count,
          first: page > 1,
          last: page_count > page,
          firstRecordCount: offset + 1,
          lastRecordCount: offset + record_count,
          startPage: start_page,
          endPage: end_page,
        }
      end
    end
  end
end
