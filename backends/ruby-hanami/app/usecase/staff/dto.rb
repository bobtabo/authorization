# frozen_string_literal: true
#
# スタッフユースケースの DTO を定義するモジュール。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

module UseCase
  module Staff
    # スタッフのロール更新時に渡す DTO です。
    UpdateRoleDto = Struct.new(:id, :role, :executor_id, :version, keyword_init: true)

    # スタッフ削除時に渡す DTO です。
    DestroyDto    = Struct.new(:id, :executor_id, :version, keyword_init: true)

    # スタッフ復元時に渡す DTO です。
    RestoreDto    = Struct.new(:id, :version, keyword_init: true)
  end
end
