# frozen_string_literal: true
#
# staff_id クッキーの HMAC 署名・検証モジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

require "openssl"

module Support
  # staff_id を HMAC-SHA256 で署名・検証するモジュールです。
  module StaffSession
    # @param staff_id [Integer] スタッフ ID
    # @param secret [String] 署名シークレット
    # @param lifetime_seconds [Integer] 有効期間（秒）
    # @return [String] 署名済みの値（"{staffId}.{expiresAtUnixSeconds}.{署名}"）
    def self.sign(staff_id, secret, lifetime_seconds)
      expires_at = Time.now.to_i + lifetime_seconds
      payload = "#{staff_id}.#{expires_at}"
      "#{payload}.#{hmac_hex(payload, secret)}"
    end

    # @param value [String, nil] クッキーの値
    # @param secret [String] 署名シークレット
    # @return [Integer] 検証済みのスタッフ ID（不正または期限切れの場合は 0）
    def self.verify(value, secret)
      return 0 if value.blank?

      id_part, exp_part, sig = value.split(".", 3)
      return 0 if id_part.blank? || exp_part.blank? || sig.blank?

      expected_sig = hmac_hex("#{id_part}.#{exp_part}", secret)
      return 0 unless sig.bytesize == expected_sig.bytesize
      return 0 unless ActiveSupport::SecurityUtils.secure_compare(sig, expected_sig)

      id = Integer(id_part, exception: false)
      expires_at = Integer(exp_part, exception: false)
      return 0 if id.nil? || expires_at.nil?
      return 0 if Time.now.to_i > expires_at

      id
    end

    def self.hmac_hex(payload, secret)
      OpenSSL::HMAC.hexdigest("SHA256", secret, payload)
    end
    private_class_method :hmac_hex
  end
end
