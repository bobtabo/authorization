"use client";

import { motion } from "framer-motion";
import { Building2, ArrowLeft, Save } from "lucide-react";
import React from "react";

import { ConfirmDialog } from "@/shared/components/confirm-dialog";
import { ConsoleFooter } from "@/shared/components/console-footer";
import { ConsoleHeader } from "@/shared/components/console-header";
import { formatCityWard } from "@/shared/lib/postcode-jp";

import { useClientEditForm } from "../hooks/useClientEditForm";

export function ClientEditForm(): React.JSX.Element {
  const {
    clientName,
    setClientName,
    postalCode,
    prefecture,
    city,
    setCity,
    cityChoiceIndex,
    setCityChoiceIndex,
    street,
    setStreet,
    building,
    setBuilding,
    tel,
    email,
    setEmail,
    saving,
    message,
    confirmOpen,
    setConfirmOpen,
    handleTelChange,
    handlePostalChange,
    postcodeLoading,
    postcodeError,
    postcodeRows,
    handleSubmit,
    handleConfirmUpdate,
  } = useClientEditForm();

  const fieldBaseClass =
    "w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent";
  const inputClass = `${fieldBaseClass} text-gray-900 placeholder:text-gray-300`;
  const readOnlyFieldClass = `${inputClass} bg-gray-50 cursor-default`;
  const citySelectClass = `${fieldBaseClass} text-gray-900 bg-white`;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <ConsoleHeader />

      <main className="flex-1">
        <div className="max-w-4xl mx-auto px-6 py-10">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-2xl font-semibold text-gray-900 flex items-center gap-2">
              <Building2 size={24} />
              クライアント編集
            </h1>
            <a
              href="/clients"
              className="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-gray-800"
            >
              <ArrowLeft size={16} />
              一覧へ戻る
            </a>
          </div>

          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden"
          >
            <form onSubmit={handleSubmit} className="px-6 py-6 space-y-5">
              {message && (
                <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3">
                  {message}
                </div>
              )}

              <div className="space-y-2">
                <label htmlFor="client-edit-name" className="block text-sm font-medium text-gray-700">
                  クライアント名
                  <span className="text-red-500 ml-0.5">*</span>
                </label>
                <input
                  id="client-edit-name"
                  type="text"
                  value={clientName}
                  onChange={(e) => setClientName(e.target.value.slice(0, 255))}
                  placeholder="株式会社モックデータ商事"
                  required
                  maxLength={255}
                  className={inputClass}
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-[minmax(0,10ch)_10em_minmax(0,1fr)] gap-4 md:items-end">
                <div className="space-y-2 w-full max-w-[10ch] md:max-w-[10ch]">
                  <label htmlFor="client-edit-postal-code" className="block text-sm font-medium text-gray-700">
                    郵便番号
                    <span className="text-red-500 ml-0.5">*</span>
                    {postcodeLoading && (
                      <span className="ml-2 text-xs font-normal text-indigo-600">
                        住所を検索中…
                      </span>
                    )}
                  </label>
                  <input
                    id="client-edit-postal-code"
                    type="text"
                    inputMode="numeric"
                    autoComplete="postal-code"
                    value={postalCode}
                    onChange={handlePostalChange}
                    placeholder="0000000"
                    required
                    maxLength={7}
                    className={inputClass}
                  />
                </div>

                <div className="space-y-2 w-full min-w-0">
                  <label htmlFor="client-edit-prefecture" className="block text-sm font-medium text-gray-700">
                    都道府県
                  </label>
                  <input
                    id="client-edit-prefecture"
                    type="text"
                    readOnly
                    value={prefecture}
                    placeholder="郵便番号で自動入力"
                    className={`${readOnlyFieldClass} w-full`}
                    aria-readonly="true"
                  />
                </div>

                <div className="space-y-2 min-w-0 w-full">
                  <label htmlFor="client-edit-city" className="block text-sm font-medium text-gray-700">
                    市区町村
                    <span className="text-red-500 ml-0.5">*</span>
                    {postcodeRows.length > 1 && (
                      <span className="ml-2 text-xs font-normal text-gray-500">
                        複数候補があります。選択してください。
                      </span>
                    )}
                  </label>
                  {postcodeRows.length > 1 ? (
                    <select
                      id="client-edit-city"
                      value={cityChoiceIndex}
                      onChange={(e) => {
                        const i = Number(e.target.value);
                        setCityChoiceIndex(i);
                        const row = postcodeRows[i];
                        if (row) setCity(formatCityWard(row));
                      }}
                      required
                      className={`${citySelectClass} w-full`}
                    >
                      {postcodeRows.map((row, i) => (
                        <option
                          key={`${row.pref}-${row.city}-${row.town}-${i}`}
                          value={i}
                        >
                          {formatCityWard(row)}
                        </option>
                      ))}
                    </select>
                  ) : (
                    <input
                      id="client-edit-city"
                      type="text"
                      value={city}
                      onChange={(e) => setCity(e.target.value.slice(0, 255))}
                      placeholder="架空市中央区みなみ町"
                      required
                      maxLength={255}
                      className={inputClass}
                    />
                  )}
                </div>
              </div>

              {postcodeError && (
                <p className="text-sm text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2">
                  {postcodeError}
                </p>
              )}

              <div className="space-y-2">
                <label htmlFor="client-edit-street" className="block text-sm font-medium text-gray-700">
                  丁目・番地
                  <span className="text-red-500 ml-0.5">*</span>
                </label>
                <input
                  id="client-edit-street"
                  type="text"
                  value={street}
                  onChange={(e) => setStreet(e.target.value.slice(0, 255))}
                  placeholder="1丁目2番3号"
                  required
                  maxLength={255}
                  className={inputClass}
                />
              </div>

              <div className="space-y-2">
                <label htmlFor="client-edit-building" className="block text-sm font-medium text-gray-700">
                  ビル名
                </label>
                <input
                  id="client-edit-building"
                  type="text"
                  value={building}
                  onChange={(e) => setBuilding(e.target.value.slice(0, 255))}
                  placeholder="サンプルプラザ東館"
                  maxLength={255}
                  className={inputClass}
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-[4fr_6fr] gap-4">
                <div className="space-y-2 min-w-0">
                  <label htmlFor="client-edit-tel" className="block text-sm font-medium text-gray-700">
                    電話番号
                    <span className="text-red-500 ml-0.5">*</span>
                  </label>
                  <input
                    id="client-edit-tel"
                    type="text"
                    inputMode="numeric"
                    autoComplete="tel"
                    value={tel}
                    onChange={handleTelChange}
                    placeholder="09000000000"
                    required
                    maxLength={255}
                    className={inputClass}
                  />
                </div>

                <div className="space-y-2 min-w-0">
                  <label htmlFor="client-edit-email" className="block text-sm font-medium text-gray-700">
                    メールアドレス
                    <span className="text-red-500 ml-0.5">*</span>
                  </label>
                  <input
                    id="client-edit-email"
                    type="email"
                    autoComplete="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value.slice(0, 255))}
                    placeholder="contact@example.com"
                    required
                    maxLength={255}
                    className={inputClass}
                  />
                </div>
              </div>

              <div className="pt-2 flex items-center justify-end gap-3">
                <a
                  href="/clients"
                  className="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 hover:border-gray-400 transition-colors"
                >
                  キャンセル
                </a>
                <button
                  type="submit"
                  disabled={saving}
                  className="inline-flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium px-4 py-2 rounded-lg disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  <Save size={16} />
                  更新
                </button>
              </div>
            </form>
          </motion.div>
        </div>
      </main>

      <ConsoleFooter />

      <ConfirmDialog
        open={confirmOpen}
        titleId="client-edit-confirm-title"
        title="更新の確認"
        onClose={() => setConfirmOpen(false)}
        closeDisabled={saving}
      >
        <p className="text-gray-600 mb-6">
          「{clientName}」の内容を更新してもよろしいですか？
        </p>

        <div className="flex gap-3 justify-end">
          <button
            type="button"
            onClick={() => setConfirmOpen(false)}
            disabled={saving}
            className="inline-flex items-center justify-center px-4 py-2 text-sm font-medium rounded-lg border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 hover:border-gray-400 transition-colors disabled:opacity-50 disabled:pointer-events-none"
          >
            キャンセル
          </button>
          <button
            type="button"
            onClick={handleConfirmUpdate}
            disabled={saving}
            className="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 disabled:opacity-50 flex items-center gap-2"
          >
            {saving && (
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            )}
            更新する
          </button>
        </div>
      </ConfirmDialog>
    </div>
  );
}
