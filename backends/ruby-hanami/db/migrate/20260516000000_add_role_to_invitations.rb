# frozen_string_literal: true
#
# invitations テーブルに role カラムを追加するマイグレーション。
#
# @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

# invitations テーブルに role（権限）カラムを追加します。
Sequel.migration do
  up do
    alter_table(:invitations) do
      add_column :role, Integer, unsigned: true, null: false, default: 2, after: :token
    end
  end

  down do
    alter_table(:invitations) do
      drop_column :role
    end
  end
end
