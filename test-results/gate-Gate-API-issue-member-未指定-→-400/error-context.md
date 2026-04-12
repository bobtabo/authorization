# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: gate.spec.ts >> Gate API >> issue >> member 未指定 → 400
- Location: tests/e2e/gate.spec.ts:29:9

# Error details

```
Error: expect(received).toBe(expected) // Object.is equality

Expected: 400
Received: 404
```

# Test source

```ts
  1   | import { test, expect } from '@playwright/test';
  2   | 
  3   | /**
  4   |  * Gate API E2E テスト
  5   |  *
  6   |  * 事前準備: tests/e2e/.env.e2e に E2E 用クライアントのクレデンシャルを設定してください。
  7   |  */
  8   | 
  9   | const accessToken = process.env.E2E_CLIENT_ACCESS_TOKEN ?? '';
  10  | const identifier  = process.env.E2E_CLIENT_IDENTIFIER ?? '';
  11  | 
  12  | test.describe('Gate API', () => {
  13  |   // -----------------------------------------------------------------------
  14  |   // JWT 発行
  15  |   // -----------------------------------------------------------------------
  16  |   test.describe('issue', () => {
  17  |     test('正常発行', async ({ request }) => {
  18  |       const res = await request.get('/api/gate/issue?member=e2e-member-001', {
  19  |         headers: { Authorization: `Bearer ${accessToken}` },
  20  |       });
  21  | 
  22  |       expect(res.status()).toBe(200);
  23  |       const body = await res.json();
  24  |       expect(body.message).toBe('SUCCESS');
  25  |       expect(typeof body.token).toBe('string');
  26  |       expect(body.token.split('.').length).toBe(3); // JWT 形式確認
  27  |     });
  28  | 
  29  |     test('member 未指定 → 400', async ({ request }) => {
  30  |       const res = await request.get('/api/gate/issue', {
  31  |         headers: { Authorization: `Bearer ${accessToken}` },
  32  |       });
  33  | 
> 34  |       expect(res.status()).toBe(400);
      |                            ^ Error: expect(received).toBe(expected) // Object.is equality
  35  |       const body = await res.json();
  36  |       expect(body.message).toBe('member を指定してください。');
  37  |     });
  38  | 
  39  |     test('不正トークン → 401', async ({ request }) => {
  40  |       const res = await request.get('/api/gate/issue?member=e2e-member-001', {
  41  |         headers: { Authorization: 'Bearer invalid-token' },
  42  |       });
  43  | 
  44  |       expect(res.status()).toBe(401);
  45  |       const body = await res.json();
  46  |       expect(body.message).toBe('クライアントが存在しません。');
  47  |     });
  48  |   });
  49  | 
  50  |   // -----------------------------------------------------------------------
  51  |   // JWT 検証
  52  |   // -----------------------------------------------------------------------
  53  |   test.describe('verify', () => {
  54  |     let jwt: string;
  55  | 
  56  |     test.beforeEach(async ({ request }) => {
  57  |       // 各テスト前に JWT を発行しておく
  58  |       const res = await request.get('/api/gate/issue?member=e2e-member-001', {
  59  |         headers: { Authorization: `Bearer ${accessToken}` },
  60  |       });
  61  |       jwt = (await res.json()).token;
  62  |     });
  63  | 
  64  |     test('正常検証', async ({ request }) => {
  65  |       const res = await request.get(
  66  |         `/api/gate/client/${identifier}/verify?token=${jwt}`,
  67  |       );
  68  | 
  69  |       expect(res.status()).toBe(200);
  70  |       const body = await res.json();
  71  |       expect(body.message).toBe('SUCCESS');
  72  |       expect(body.iss).toBe('authorization');
  73  |       expect(body.aud).toBe(identifier);
  74  |       expect(body.sub).toBe('e2e-member-001');
  75  |     });
  76  | 
  77  |     test('token 未指定 → 400', async ({ request }) => {
  78  |       const res = await request.get(`/api/gate/client/${identifier}/verify`);
  79  | 
  80  |       expect(res.status()).toBe(400);
  81  |       const body = await res.json();
  82  |       expect(body.message).toBe('token を指定してください。');
  83  |     });
  84  | 
  85  |     test('不明 identifier → 403', async ({ request }) => {
  86  |       const res = await request.get(
  87  |         `/api/gate/client/unknown-client/verify?token=dummy`,
  88  |       );
  89  | 
  90  |       expect(res.status()).toBe(403);
  91  |       const body = await res.json();
  92  |       expect(body.message).toBe('クライアントが存在しません。');
  93  |     });
  94  | 
  95  |     test('無効 JWT → 401', async ({ request }) => {
  96  |       const res = await request.get(
  97  |         `/api/gate/client/${identifier}/verify?token=invalid.jwt.token`,
  98  |       );
  99  | 
  100 |       expect(res.status()).toBe(401);
  101 |       const body = await res.json();
  102 |       expect(body.message).toBe('JWT が無効です。');
  103 |     });
  104 |   });
  105 | });
  106 | 
```