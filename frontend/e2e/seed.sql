-- E2E real-backend テスト用シードデータ
-- 実行前提: バックエンドコンテナが起動済みであること
--
-- 使い方（バックエンドの DB コンテナに対して実行）:
--   mysql -h 127.0.0.1 -P 3306 -u root -p'docker#DOCKER1234' authorization < frontend/e2e/seed.sql
--
-- ・auth/me と notifications は常にモックのため、staffs/notifications の件数は任意
-- ・clients: mockClients / mockClientDetail / mockClientsWithDeleted の値に一致させる
-- ・invitations: token='current-token-abc123' を使う（invitation.spec.ts の期待値）

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE notifications;
TRUNCATE TABLE invitations;
TRUNCATE TABLE clients;
TRUNCATE TABLE staffs;
SET FOREIGN_KEY_CHECKS = 1;

-- staffs （mockStaffs に合わせる）
INSERT INTO staffs
  (id, name, email, provider, provider_id, avatar, role, created_at, created_by, updated_at, updated_by, version)
VALUES
  (1, 'テストスタッフ',   'staff@example.com',  1, 'e2e-provider-001', NULL, 1, '2026-01-01 00:00:00', 0, '2026-01-01 00:00:00', 0, 1),
  (2, 'メンバースタッフ', 'member@example.com', 1, 'e2e-provider-002', NULL, 2, '2026-02-01 00:00:00', 0, '2026-02-01 00:00:00', 0, 1);

-- clients
--   id=1: 株式会社サンプル  status=2(利用中)  → mockClientDetail に一致
--   id=2: テスト商事        status=1(準備中)
--   id=3: アーカイブ商事   status=4(archived) → mockSoftDeletedClientDetail に一致
SET @priv = '-----BEGIN RSA PRIVATE KEY-----\nMIIEogIBAAKCAQEA41XpNMF/j1s71eZh7pvldfsPgo8i4xlGJrmPtYEXY2fCwybm\nt8m/GQsW2KUgz7vX/1jlUGFjprvuSnCzMIzaI0lsuIY1kQWMyS/UJAYazFcgoM3D\nwx01I7rxLvp8Io9W+WihLTmMJVrYz9oQJKdBZl8vlZhiGbcQ3NI4ikEnCR8QHxyD\nUkvqZ4BoXhDb5CcuPkg24CGqM/7hjulnHCZ871q3jkw3DDs5eiXwNPRmqVx9nUGU\nKGD0Sw1v97SFo3ohGeUhLyTqkjF1Rbx7JWIi+lMR+GvFjbHibR4G7+Ge1NKFOqUu\nl3shd5qf937p3pvW3rEdPg+dynqi5lHN3O8/pQIDAQABAoIBABTMsUkVOSK4d17d\n33DxjOArXSMWDdxBsuRBJGUNZMKWEEDtsryhtfJzIXR8leYbwCqfNgqRgTNp92aS\ngs/lheHQ9SRkNyDH0AvqQdpylTTD9frFyMGgeaewEiK5DUQrjDVxPWnsstC0y/3R\nwZoGPtrAc73tUsXFNO2XQvq3SlHa/GcggYP/FacAbq/lnI3sKYDV6LXytrVxrTcJ\nPO+7gJfmouzRjrachLL91TXuRN9YyLyDVrRAWM52f0NA/8oxhSH/Mrc1OW3IrqDF\ny16cUZnI2mAg8JLPIwBWW2eAciHB9mMMx8supkit+ey3R9Pwkma7Ds0qaflbSvZo\n/d/JN9MCgYEA9HCjlxOzsOa0p7e6K8cn877d/+i4bm6j4dRwLLsq1rNYTos+ADjD\nZkFl2ncbjhK5JYMLhZhrLgQ/6JphifOjbFIei1qECkqx/ySbo4j4t3p2htsrP3yJ\nVGrpL8rBZpER801qq7KZ5ITNjabz9kErS1Bgp4fI2dWSlWBKXKIU2YsCgYEA7hYx\nrpDeYsGJo9iDOhduL6V8xwlA1MlRENjCgZnIZUXInCsWxd88DfYEsUzJiNEkt6yg\neCl2xZSK7HeiP+zHNzd93H45vjdee/AODtUhOPlQQi33reAofArt28iJRQdQns40\nSrI51FdiTcPM8LsaGpMnma+043XaJG82C3p2kY8CgYANe7UaXUTZKjCm27SSO58R\ni+K0/gJGXSX/C/fQD9byFtx1IKsf2gG1P0A+B7nmYVQDJLJbAShQVn2r9/APavgF\nvpXeu/RymOIun7dSEvkdLc0h7S76hoUQugD42OdIIBJsaEXNCMICX+zytlXYrImV\n0u4x74R9t5EpWjFf4LTjtwKBgGM8NPEByO94o5n7QJcR9Qc+/scnADKwxm0zK79B\nDdnIQFl3TEKlTS60gDg2PTodkiRMe4YaIjswMsdlBeeHFXHaW0dwBTlXcrZN1E4y\nX2qT3/P1nOqIJ9er5oBZEX4IEn5ejEUmDByJX5vyJJWiJrs04qiYJ9k24fmVsP0P\nyiPZAoGAI01V863Vy6b50yA8pU8qnG243SEgt9+/f1yqTB6IrytNJnkn1Lpynu+c\n9erwLx/Iu4SrvJmIsHNNY2JZAS+WBTLdu4QqlYORzGBJDRZjLdFD1dOAX6fbau01\n1aPKqDmgvvW2bxtHmlXzmP5S3s/9gdGz5TOvRvl3W5eWhkbT9zQ=\n-----END RSA PRIVATE KEY-----';

SET @pub  = '-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA41XpNMF/j1s71eZh7pvl\ndfsPgo8i4xlGJrmPtYEXY2fCwybmt8m/GQsW2KUgz7vX/1jlUGFjprvuSnCzMIza\nI0lsuIY1kQWMyS/UJAYazFcgoM3Dwx01I7rxLvp8Io9W+WihLTmMJVrYz9oQJKdB\nZl8vlZhiGbcQ3NI4ikEnCR8QHxyDUkvqZ4BoXhDb5CcuPkg24CGqM/7hjulnHCZ8\n71q3jkw3DDs5eiXwNPRmqVx9nUGUKGD0Sw1v97SFo3ohGeUhLyTqkjF1Rbx7JWIi\n+lMR+GvFjbHibR4G7+Ge1NKFOqUul3shd5qf937p3pvW3rEdPg+dynqi5lHN3O8/\npQIDAQAB\n-----END PUBLIC KEY-----';

INSERT INTO clients
  (id, name, identifier, post_code, pref, city, address, building, tel, email,
   access_token, private_key, public_key, fingerprint,
   status, start_at, stop_at,
   created_at, created_by, updated_at, updated_by, deleted_at, version)
VALUES
  (1, '株式会社サンプル', 'e2e-client-001', '1000001', '東京都', '千代田区', '千代田1-1', '', '0312345678', 'sample@example.com',
   'e2eaccesstoken00000000000000000000000000000000000000000000000001',
   @priv, @pub, 'SHA256:test',
   2, '2026-01-15 09:00:00', NULL,
   '2026-01-01 00:00:00', 0, '2026-01-15 09:00:00', 0, NULL, 1),

  (2, 'テスト商事', 'e2e-client-002', '1000002', '東京都', '新宿区', '新宿1-1', '', '0312345679', 'test@example.com',
   'e2eaccesstoken00000000000000000000000000000000000000000000000002',
   @priv, @pub, 'SHA256:test',
   1, NULL, NULL,
   '2026-02-01 00:00:00', 0, '2026-02-01 00:00:00', 0, NULL, 1),

  (3, 'アーカイブ商事', 'e2e-client-003', '1000001', '東京都', '千代田区', '千代田1-1', '', '0312345678', 'archive@example.com',
   'e2eaccesstoken00000000000000000000000000000000000000000000000003',
   @priv, @pub, 'SHA256:test',
   4, '2025-01-01 09:00:00', '2025-12-31 18:00:00',
   '2025-01-01 00:00:00', 0, '2026-01-01 00:00:00', 0, '2026-01-01 00:00:00', 1);

-- invitations（invitation.spec.ts の current-token-abc123 に一致させる）
INSERT INTO invitations
  (token, created_at, updated_at)
VALUES
  ('current-token-abc123', '2026-01-01 00:00:00', '2026-01-01 00:00:00');
