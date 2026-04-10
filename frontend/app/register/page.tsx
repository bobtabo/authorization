"use client";

import React, { useState } from "react";
import { motion } from "framer-motion";
import { Mail, Lock, Eye, EyeOff, LogIn, Loader2, ShieldCheck } from "lucide-react";

export default function LoginPage(): React.JSX.Element {
  const [email, setEmail] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    // Simulate login
    setTimeout(() => {
      if (email === "admin@example.com" && password === "password") {
        alert("ログイン成功！");
      } else {
        setError("メールアドレスまたはパスワードが正しくありません");
      }
      setLoading(false);
    }, 1000);
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Header */}
      <header className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-6 h-14 flex items-center">
          <div className="flex items-center gap-3">
            <div className="w-9 h-8 rounded-lg bg-indigo-600 text-white flex items-center justify-center">
              <ShieldCheck size={16} />
            </div>
            <span className="text-sm font-semibold text-gray-800">
              Authorization Gateway
            </span>
          </div>
        </div>
      </header>

      {/* Main */}
      <main className="flex-1 flex items-center justify-center px-6 py-12">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className="w-full max-w-md"
        >
          <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
            {/* Card Header */}
            <div className="bg-indigo-50 border-b border-indigo-100 px-8 py-6">
              <h1 className="text-xl font-semibold text-gray-900">
                ログイン
              </h1>
              <p className="text-sm text-gray-600 mt-1">
                アカウントにサインインしてください
              </p>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit} className="px-8 py-6 space-y-5">
              {/* Error Message */}
              {error && (
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-lg"
                >
                  {error}
                </motion.div>
              )}

              {/* Email Field */}
              <div className="space-y-2">
                <label
                  htmlFor="email"
                  className="block text-sm font-medium text-gray-700"
                >
                  メールアドレス
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Mail size={18} className="text-gray-400" />
                  </div>
                  <input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@example.com"
                    required
                    className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
                  />
                </div>
              </div>

              {/* Password Field */}
              <div className="space-y-2">
                <label
                  htmlFor="password"
                  className="block text-sm font-medium text-gray-700"
                >
                  パスワード
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Lock size={18} className="text-gray-400" />
                  </div>
                  <input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    className="w-full pl-10 pr-12 py-2.5 border border-gray-300 rounded-lg text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              </div>

              {/* Forgot Password */}
              <div className="flex justify-center text-sm">
                <a
                  href="#"
                  className="text-indigo-600 hover:text-indigo-700 font-medium"
                >
                  パスワードを忘れた方
                </a>
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                disabled={loading}
                className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2.5 px-4 rounded-lg flex items-center justify-center gap-2 transition disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <>
                    <Loader2 size={18} className="animate-spin" />
                    ログイン中...
                  </>
                ) : (
                  <>
                    <LogIn size={18} />
                    ログイン
                  </>
                )}
              </button>

              {/* Divider */}
              <div className="relative my-6">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-200" />
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="bg-white px-4 text-gray-500">または</span>
                </div>
              </div>

              {/* Sign Up Link */}
              <p className="text-center text-sm text-gray-600">
                アカウントをお持ちでない方は{" "}
                <a
                  href="/register"
                  className="text-indigo-600 hover:text-indigo-700 font-medium"
                >
                  新規登録
                </a>
              </p>
            </form>
          </div>
        </motion.div>
      </main>

      {/* Footer */}
      <footer className="border-t border-gray-200 bg-white py-6 text-center text-xs text-gray-400">
        © 2026 My SaaS Dashboard. All rights reserved.
      </footer>
    </div>
  );
}
