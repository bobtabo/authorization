"use client";

import React, { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Building2, ArrowLeft, Save, X } from "lucide-react";
import { ConsoleHeader } from "@/components/console-header";
import { ConsoleFooter } from "@/components/console-footer";
import { usePostcodeJpLookup } from "@/hooks/use-postcode-jp-lookup";
import { formatCityWard } from "@/lib/postcode-jp";

/** モック: 既存データとして読み込んだ想定の初期値（必須項目は入力済み） */
const DEFAULT_EDIT_VALUES = {
  clientName: "株式会社ストラテジックインサイト",
  postalCode: "1070061",
  prefecture: "東京都",
  city: "港区北青山",
  street: "二丁目5番8号",
  building: "青山OMスクエア",
  tel: "0312345678",
  email: "contact@sii-japan.co.jp",
} as const;

export default function ClientEditPage(): React.JSX.Element {
  const [clientName, setClientName] = useState<string>(DEFAULT_EDIT_VALUES.clientName);
  const [postalCode, setPostalCode] = useState<string>(DEFAULT_EDIT_VALUES.postalCode);
  const [prefecture, setPrefecture] = useState<string>(DEFAULT_EDIT_VALUES.prefecture);
  const [city, setCity] = useState<string>(DEFAULT_EDIT_VALUES.city);
  const [cityChoiceIndex, setCityChoiceIndex] = useState<number>(0);
  const [street, setStreet] = useState<string>(DEFAULT_EDIT_VALUES.street);
  const [building, setBuilding] = useState<string>(DEFAULT_EDIT_VALUES.building);
  const [tel, setTel] = useState<string>(DEFAULT_EDIT_VALUES.tel);
  const [email, setEmail] = useState<string>(DEFAULT_EDIT_VALUES.email);
  const [saving, setSaving] = useState<boolean>(false);
  const [message, setMessage] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState<boolean>(false);

  const handleTelChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const digitsOnly = e.target.value.replace(/\D/g, "");
    setTel(digitsOnly.slice(0, 255));
  };

  const handlePostalChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const v = e.target.value.replace(/\D/g, "");
    setPostalCode(v.slice(0, 7));
  };

  const { loading: postcodeLoading, error: postcodeError, rows: postcodeRows } =
    usePostcodeJpLookup(postalCode);

  useEffect(() => {
    if (postcodeRows.length === 0) {
      const digits = postalCode.replace(/\D/g, "");
      if (digits.length < 7) {
        setPrefecture("");
        setCity("");
        setCityChoiceIndex(0);
      }
      return;
    }
    setPrefecture(postcodeRows[0].pref);
    setCityChoiceIndex(0);
    setCity(formatCityWard(postcodeRows[0]));
  }, [postcodeRows, postalCode]);

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = e.currentTarget;
    if (!form.checkValidity()) {
      form.reportValidity();
      return;
    }
    setConfirmOpen(true);
  };

  const handleConfirmUpdate = () => {
    setSaving(true);
    setMessage(null);

    // Draft: replace with API call later.
    setTimeout(() => {
      setSaving(false);
      setConfirmOpen(false);
      setMessage("更新しました（モック）");
    }, 800);
  };

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
                <div className="bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-lg px-4 py-3">
                  {message}
                </div>
              )}

              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">
                  クライアント名
                  <span className="text-red-500 ml-0.5">*</span>
                </label>
                <input
                  type="text"
                  value={clientName}
                  onChange={(e) => setClientName(e.target.value.slice(0, 255))}
                  placeholder="株式会社ストラテジックインサイト"
                  required
                  maxLength={255}
                  className={inputClass}
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-[minmax(0,10ch)_10em_minmax(0,1fr)] gap-4 md:items-end">
                <div className="space-y-2 w-full max-w-[10ch] md:max-w-[10ch]">
                  <label className="block text-sm font-medium text-gray-700">
                    郵便番号
                    <span className="text-red-500 ml-0.5">*</span>
                    {postcodeLoading && (
                      <span className="ml-2 text-xs font-normal text-indigo-600">
                        住所を検索中…
                      </span>
                    )}
                  </label>
                  <input
                    type="text"
                    inputMode="numeric"
                    autoComplete="postal-code"
                    value={postalCode}
                    onChange={handlePostalChange}
                    placeholder="1070061"
                    required
                    maxLength={7}
                    className={inputClass}
                  />
                </div>

                <div className="space-y-2 w-full min-w-0">
                  <label className="block text-sm font-medium text-gray-700">
                    都道府県
                  </label>
                  <input
                    type="text"
                    readOnly
                    value={prefecture}
                    placeholder="郵便番号から自動入力されます"
                    className={`${readOnlyFieldClass} w-full`}
                    aria-readonly="true"
                  />
                </div>

                <div className="space-y-2 min-w-0 w-full">
                  <label className="block text-sm font-medium text-gray-700">
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
                      type="text"
                      value={city}
                      onChange={(e) => setCity(e.target.value.slice(0, 255))}
                      placeholder="港区北青山"
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
                <label className="block text-sm font-medium text-gray-700">
                  丁目・番地
                  <span className="text-red-500 ml-0.5">*</span>
                </label>
                <input
                  type="text"
                  value={street}
                  onChange={(e) => setStreet(e.target.value.slice(0, 255))}
                  placeholder="二丁目5番8号"
                  required
                  maxLength={255}
                  className={inputClass}
                />
              </div>

              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">
                  ビル名
                </label>
                <input
                  type="text"
                  value={building}
                  onChange={(e) => setBuilding(e.target.value.slice(0, 255))}
                  placeholder="青山OMスクエア"
                  maxLength={255}
                  className={inputClass}
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-[4fr_6fr] gap-4">
                <div className="space-y-2 min-w-0">
                  <label className="block text-sm font-medium text-gray-700">
                    電話番号
                    <span className="text-red-500 ml-0.5">*</span>
                  </label>
                  <input
                    type="text"
                    inputMode="numeric"
                    autoComplete="tel"
                    value={tel}
                    onChange={handleTelChange}
                    placeholder="0312345678"
                    required
                    maxLength={255}
                    className={inputClass}
                  />
                </div>

                <div className="space-y-2 min-w-0">
                  <label className="block text-sm font-medium text-gray-700">
                    メールアドレス
                    <span className="text-red-500 ml-0.5">*</span>
                  </label>
                  <input
                    type="email"
                    autoComplete="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value.slice(0, 255))}
                    placeholder="example@company.co.jp"
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

      <AnimatePresence>
        {confirmOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
            onClick={() => !saving && setConfirmOpen(false)}
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
              className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4"
            >
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold text-gray-900">
                  更新の確認
                </h2>
                <button
                  type="button"
                  onClick={() => !saving && setConfirmOpen(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <X size={20} />
                </button>
              </div>

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
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
