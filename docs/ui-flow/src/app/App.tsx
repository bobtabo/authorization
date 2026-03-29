import { ArrowRight, ArrowDown, ArrowLeft } from "lucide-react";
import { flowImages } from "./flowImages";

export default function App() {
  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-7xl mx-auto">
        <div className="flex flex-col items-center gap-6">
          {/* 最初のアクセスポイント */}
          <div className="flex gap-8 items-start">
            {/* 通常アクセス */}
            <div className="flex flex-col items-center gap-4">
              <div className="bg-blue-100 border-2 border-blue-400 rounded-lg p-4 w-48 text-center">
                <div className="font-bold text-blue-900">通常アクセス</div>
                <div className="text-sm text-blue-700 mt-1">/auth/login</div>
              </div>
              <ArrowDown className="text-gray-400" size={32} />
            </div>

            {/* 招待URL */}
            <div className="flex flex-col items-center gap-4">
              <div className="bg-green-100 border-2 border-green-400 rounded-lg p-4 w-48 text-center">
                <div className="font-bold text-green-900">招待URLアクセス</div>
                <div className="text-sm text-green-700 mt-1">/auth/invitation/{'{token}'}</div>
              </div>
              <ArrowDown className="text-gray-400" size={32} />
            </div>
          </div>

          {/* ログインページ */}
          <div className="flex flex-col items-center gap-4 relative">
            <div className="bg-white border-4 border-purple-500 rounded-lg p-6 w-80 shadow-lg">
              <div className="text-center mb-3">
                <div className="text-lg font-bold text-purple-900">ログインページ</div>
              </div>
              <img
                src={flowImages.login}
                alt="ログイン画面"
                className="w-full h-auto rounded"
              />
              <div className="mt-3 p-2 bg-purple-50 rounded text-xs">
                <div className="font-semibold text-purple-900">GoogleアカウントSSO認証</div>
              </div>
            </div>

            {/* ログアウトからの矢印 */}
            <div className="absolute -right-32 top-1/2 -translate-y-1/2 flex items-center gap-2">
              <ArrowLeft className="text-red-500" size={28} />
              <div className="text-xs font-semibold text-red-600">ログアウト</div>
            </div>

            {/* ログインからクライアント一覧への矢印 */}
            <div className="flex flex-col items-center">
              <ArrowDown className="text-indigo-500" size={32} strokeWidth={3} />
              <div className="text-xs font-semibold text-indigo-600">ログイン成功</div>
            </div>
          </div>

          {/* 共通ヘッダー */}
          <div className="w-full max-w-5xl bg-white border-4 border-slate-600 rounded-lg shadow-lg p-4 relative">
            <div className="text-center mb-3">
              <div className="text-lg font-bold text-slate-900">共通ヘッダー</div>
              <div className="text-xs text-gray-600">全ページ共通</div>
            </div>
            <img
              src={flowImages.header}
              alt="ヘッダー画面"
              className="w-full h-auto rounded"
            />

            {/* ヘッダーからクライアント一覧への矢印 */}
            <div className="absolute -bottom-12 left-1/3 flex flex-col items-center">
              <ArrowDown className="text-indigo-500" size={28} strokeWidth={3} />
              <div className="text-xs font-semibold text-indigo-600 mt-1">クライアント</div>
            </div>

            {/* ヘッダーからスタッフページへの矢印 */}
            <div className="absolute -bottom-12 right-1/3 flex flex-col items-center">
              <ArrowDown className="text-cyan-500" size={28} strokeWidth={3} />
              <div className="text-xs font-semibold text-cyan-600 mt-1">スタッフ</div>
            </div>
          </div>

          {/* メイン画面エリア */}
          <div className="w-full max-w-5xl mt-8">
            <div className="flex gap-8 justify-center">
              {/* クライアント一覧（TOPページ） */}
              <div className="flex flex-col items-center gap-4 relative">
                {/* ログイン成功の横矢印 */}
                <div className="absolute -left-32 top-8 flex items-center gap-2">
                  <div className="text-xs font-semibold text-indigo-600">ログイン成功</div>
                  <ArrowRight className="text-indigo-500" size={28} strokeWidth={3} />
                </div>

                <div className="bg-white border-4 border-indigo-500 rounded-lg p-6 w-80 shadow-lg">
                  <div className="text-center mb-3">
                    <div className="text-lg font-bold text-indigo-900">クライアント一覧</div>
                    <div className="text-xs text-gray-600">TOPページ</div>
                  </div>
                  <img
                    src={flowImages.clientList}
                    alt="クライアント一覧画面"
                    className="w-full h-auto rounded"
                  />
                </div>

                {/* クライアント一覧からの遷移 */}
                <div className="flex gap-4 mt-2">
                  {/* 登録ページ */}
                  <div className="flex flex-col items-center gap-2">
                    <ArrowDown className="text-gray-400" size={24} />
                    <div className="bg-white border-3 border-emerald-500 rounded-lg p-4 w-48 shadow">
                      <div className="text-center mb-2">
                        <div className="text-sm font-bold text-emerald-900">登録</div>
                      </div>
                      <img
                        src={flowImages.clientRegister}
                        alt="クライアント登録画面"
                        className="w-full h-auto rounded"
                      />
                    </div>
                  </div>

                  {/* 編集ページ */}
                  <div className="flex flex-col items-center gap-2">
                    <ArrowDown className="text-gray-400" size={24} />
                    <div className="bg-white border-3 border-amber-500 rounded-lg p-4 w-48 shadow">
                      <div className="text-center mb-2">
                        <div className="text-sm font-bold text-amber-900">編集</div>
                      </div>
                      <img
                        src={flowImages.clientEdit}
                        alt="クライアント編集画面"
                        className="w-full h-auto rounded"
                      />
                    </div>
                  </div>

                  {/* 詳細ページ */}
                  <div className="flex flex-col items-center gap-2">
                    <ArrowDown className="text-gray-400" size={24} />
                    <div className="bg-white border-3 border-rose-500 rounded-lg p-4 w-48 shadow">
                      <div className="text-center mb-2">
                        <div className="text-sm font-bold text-rose-900">詳細</div>
                      </div>
                      <img
                        src={flowImages.clientDetail}
                        alt="クライアント詳細画面"
                        className="w-full h-auto rounded"
                      />
                    </div>
                  </div>
                </div>
              </div>

              {/* スタッフページ */}
              <div className="flex flex-col items-center">
                <div className="bg-white border-4 border-cyan-500 rounded-lg p-6 w-80 shadow-lg">
                  <div className="text-center mb-3">
                    <div className="text-lg font-bold text-cyan-900">スタッフページ</div>
                  </div>
                  <img
                    src={flowImages.staffList}
                    alt="スタッフ一覧画面"
                    className="w-full h-auto rounded"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
