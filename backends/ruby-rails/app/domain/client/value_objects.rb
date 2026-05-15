# frozen_string_literal: true
#
# クライアントの値オブジェクトを定義するドメインオブジェクトモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module Domain
  module Client
    # クライアント一覧の表示用値オブジェクトです。
    ListItem = Struct.new(:id, :name, :status, :start_at, :stop_at, :created_at, :updated_at, keyword_init: true)

    # クライアント詳細の値オブジェクトです。
    DetailVo = Struct.new(
      :id, :name, :identifier,
      :post_code, :pref, :city, :address, :building, :tel, :email,
      :status, :start_at, :stop_at, :created_at, :updated_at,
      keyword_init: true
    )

    # クライアント登録結果の値オブジェクトです。
    StoreResultVo = Struct.new(:id, :name, :email, :access_token, keyword_init: true)

    # QRコードデータの値オブジェクトです。
    QrVo = Struct.new(:identifier, :deeplink_url, keyword_init: true)

    # クライアント情報（スマホアプリ向け）の値オブジェクトです。
    InfoVo = Struct.new(:identifier, :name, :status, keyword_init: true)

    # 利用開始結果の値オブジェクトです。
    StartVo = Struct.new(:access_token, keyword_init: true)
  end
end
