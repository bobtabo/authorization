# frozen_string_literal: true
#
# invitations テーブルに role カラムを追加するマイグレーション。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

# invitations テーブルに role（権限）カラムを追加します。
class AddRoleToInvitations < ActiveRecord::Migration[8.1]
  def change
    add_column :invitations, :role, :integer, unsigned: true, null: false, default: 2, after: :token, comment: "権限"
  end
end
