/**
 * Laravel `backends/php-laravel/routes/api.php`（フレームワーク既定で /api のみ。/v1 は付けない）向けの API は、
 * このモジュールの関数経由で行います（`VITE_API_URL` + 開発時は Vite の `/function` プロキシ）。
 */
export { apiClient } from "./client";
export * from "./http";
export * from "./auth";
export * from "./clients";
export * from "./staff";
export * from "./invitation";
export * from "./gate";
export * from "./notifications";
