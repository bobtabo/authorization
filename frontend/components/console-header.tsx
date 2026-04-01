import React, { useState, useEffect, useRef } from "react";
import { Link, NavLink } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import {
  Bell,
  LogOut,
  ChevronDown,
  ShieldCheck,
  Settings,
  Link2,
} from "lucide-react";
import { InvitationUrlModal } from "@/components/invitation-url-modal";
import { UserAvatar } from "@/components/user-avatar";
import { SESSION_USER_DISPLAY_NAME } from "@/lib/session-user";

interface NotificationItem {
  id: number;
  title: string;
  detail: string;
  tone: "info" | "warn" | "ok";
  unread: boolean;
}

export function ConsoleHeader(): React.JSX.Element {
  const [accountMenuOpen, setAccountMenuOpen] = useState<boolean>(false);
  const [settingsMenuOpen, setSettingsMenuOpen] = useState<boolean>(false);
  const [invitationModalOpen, setInvitationModalOpen] = useState<boolean>(false);
  const [notificationOpen, setNotificationOpen] = useState<boolean>(false);
  const [showUnreadOnly, setShowUnreadOnly] = useState<boolean>(false);
  const [notifications, setNotifications] = useState<NotificationItem[]>([
    {
      id: 1,
      title: "新しいクライアントが登録されました",
      detail: "株式会社ABC / 2分前",
      tone: "info",
      unread: true,
    },
    {
      id: 2,
      title: "停止中クライアントが1件あります",
      detail: "JKLホールディングス / 15分前",
      tone: "warn",
      unread: true,
    },
    {
      id: 3,
      title: "定期バックアップが完了しました",
      detail: "本日 09:30",
      tone: "ok",
      unread: false,
    },
  ]);

  const accountMenuRef = useRef<HTMLDivElement | null>(null);
  const settingsMenuRef = useRef<HTMLDivElement | null>(null);
  const notificationRef = useRef<HTMLDivElement | null>(null);
  const unreadCount = notifications.filter((n) => n.unread).length;
  const visibleNotifications = showUnreadOnly
    ? notifications.filter((n) => n.unread)
    : notifications;

  useEffect(() => {
    if (!accountMenuOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      if (
        accountMenuRef.current &&
        !accountMenuRef.current.contains(event.target as Node)
      ) {
        setAccountMenuOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [accountMenuOpen]);

  useEffect(() => {
    if (!notificationOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      if (
        notificationRef.current &&
        !notificationRef.current.contains(event.target as Node)
      ) {
        setNotificationOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [notificationOpen]);

  useEffect(() => {
    if (!settingsMenuOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      if (
        settingsMenuRef.current &&
        !settingsMenuRef.current.contains(event.target as Node)
      ) {
        setSettingsMenuOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [settingsMenuOpen]);

  const navTabClass = ({ isActive }: { isActive: boolean }): string =>
    [
      "inline-block pb-2.5 pt-1 text-xs font-semibold tracking-wide transition-colors border-b-2 -mb-px",
      isActive
        ? "text-violet-600 border-violet-600"
        : "text-gray-500 border-transparent hover:text-gray-700",
    ].join(" ");

  return (
    <>
    <header className="bg-white border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-6">
        <div className="h-14 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-9 h-8 rounded-lg bg-indigo-600 text-white flex items-center justify-center">
            <ShieldCheck size={16} />
          </div>
          <span className="text-sm font-semibold text-gray-800">
            Authorization Console
          </span>
        </div>

        <div className="flex items-center gap-2">
          <div ref={settingsMenuRef} className="relative">
            <button
              type="button"
              aria-label="設定メニューを開く"
              aria-expanded={settingsMenuOpen}
              aria-haspopup="menu"
              onClick={() => {
                setNotificationOpen(false);
                setAccountMenuOpen(false);
                setInvitationModalOpen(false);
                setSettingsMenuOpen((prev) => !prev);
              }}
              className="relative rounded-lg p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 transition-colors"
            >
              <Settings size={18} aria-hidden />
            </button>

            <AnimatePresence>
              {settingsMenuOpen && (
                <motion.div
                  initial={{ opacity: 0, y: -6 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -6 }}
                  className="absolute right-0 top-full mt-2 w-56 rounded-xl border border-gray-200 bg-white shadow-lg overflow-hidden z-20"
                >
                  <button
                    type="button"
                    onClick={() => {
                      setSettingsMenuOpen(false);
                      setInvitationModalOpen(true);
                    }}
                    className="flex w-full items-center gap-2 px-4 py-3 text-left text-sm text-gray-700 hover:bg-gray-50"
                  >
                    <Link2 size={15} aria-hidden />
                    <span>招待URL</span>
                  </button>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          <div ref={notificationRef} className="relative">
            <button
              type="button"
              onClick={() => {
                setSettingsMenuOpen(false);
                setInvitationModalOpen(false);
                setAccountMenuOpen(false);
                setNotificationOpen((prev) => !prev);
              }}
              disabled={unreadCount === 0}
              aria-disabled={unreadCount === 0}
              className="relative rounded-lg p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Bell size={18} />
              {unreadCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 min-w-4 h-4 px-1 bg-red-500 text-white text-[10px] leading-4 text-center rounded-full font-semibold">
                  {unreadCount}
                </span>
              )}
            </button>

            <AnimatePresence>
              {notificationOpen && (
                <motion.div
                  initial={{ opacity: 0, y: -6 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -6 }}
                  className="absolute right-0 top-full mt-2 w-80 rounded-xl border border-gray-200 bg-white shadow-lg overflow-hidden z-20"
                >
                  <div className="px-4 py-3 border-b border-gray-100 flex items-center justify-between">
                    <p className="text-sm font-semibold text-gray-800">通知</p>
                    <div className="flex items-center gap-3">
                      <button
                        type="button"
                        onClick={() => setShowUnreadOnly((prev) => !prev)}
                        className={`text-xs font-medium ${
                          showUnreadOnly
                            ? "text-indigo-700"
                            : "text-gray-500 hover:text-gray-700"
                        }`}
                      >
                        未読
                      </button>
                      <button
                        type="button"
                        onClick={() =>
                          setNotifications((prev) =>
                            prev.map((item) => ({ ...item, unread: false }))
                          )
                        }
                        className="text-xs text-indigo-600 hover:text-indigo-700 font-medium disabled:text-gray-400"
                        disabled={unreadCount === 0}
                      >
                        全既読
                      </button>
                      <span className="text-xs text-gray-500">未読 {unreadCount}件</span>
                    </div>
                  </div>
                  <div className="max-h-80 overflow-y-auto">
                    {visibleNotifications.length === 0 ? (
                      <div className="px-4 py-8 text-center text-sm text-gray-500">
                        {showUnreadOnly ? "未読なし" : "通知なし"}
                      </div>
                    ) : (
                      visibleNotifications.map((item) => (
                        <div
                          key={item.id}
                          className={`px-4 py-3 border-b border-gray-100 last:border-b-0 hover:bg-gray-50 ${
                            item.unread ? "" : "opacity-60"
                          }`}
                        >
                          <div className="flex items-start gap-2">
                            <span
                              className={`mt-1 h-2 w-2 rounded-full ${
                                item.tone === "warn"
                                  ? "bg-amber-500"
                                  : item.tone === "ok"
                                    ? "bg-emerald-500"
                                    : "bg-indigo-500"
                              }`}
                            />
                            <div className="min-w-0">
                              <p className="text-sm text-gray-800">{item.title}</p>
                              <p className="text-xs text-gray-500 mt-1">{item.detail}</p>
                            </div>
                            {item.unread && (
                              <span className="ml-auto inline-block px-1.5 py-0.5 text-[10px] rounded bg-indigo-100 text-indigo-700">
                                NEW
                              </span>
                            )}
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                  <div className="px-4 py-2 bg-gray-50 border-t border-gray-100">
                    <a
                      href="#"
                      className="text-xs text-indigo-600 hover:text-indigo-700 font-medium"
                    >
                      全て表示
                    </a>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          <div ref={accountMenuRef} className="relative">
            <button
              type="button"
              onClick={() => {
                setNotificationOpen(false);
                setSettingsMenuOpen(false);
                setInvitationModalOpen(false);
                setAccountMenuOpen((prev) => !prev);
              }}
              className="flex items-center gap-2 rounded-lg px-2 py-1.5 hover:bg-gray-100 transition-colors"
            >
              <UserAvatar name={SESSION_USER_DISPLAY_NAME} />
              <span className="text-sm text-gray-700">{SESSION_USER_DISPLAY_NAME}</span>
              <ChevronDown
                size={16}
                className={`text-gray-500 transition-transform duration-200 ${accountMenuOpen ? "rotate-180" : ""}`}
              />
            </button>

            <AnimatePresence>
              {accountMenuOpen && (
                <motion.div
                  initial={{ opacity: 0, y: -6 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -6 }}
                  className="absolute right-0 top-full mt-2 w-48 rounded-xl border border-gray-200 bg-white shadow-lg overflow-hidden z-20"
                >
                  <Link
                    to="/login"
                    onClick={() => setAccountMenuOpen(false)}
                    className="flex items-center gap-2 px-4 py-3 text-sm text-gray-700 hover:bg-gray-50"
                  >
                    <LogOut size={15} />
                    <span>ログアウト</span>
                  </Link>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
        </div>

        <nav
          className="flex items-center gap-3 sm:gap-8"
          aria-label="メインナビゲーション"
        >
          <NavLink to="/clients" className={navTabClass} end={false}>
            クライアント
          </NavLink>
          <NavLink to="/staffs" className={navTabClass} end>
            スタッフ
          </NavLink>
        </nav>
      </div>
    </header>

    <InvitationUrlModal
      open={invitationModalOpen}
      onClose={() => setInvitationModalOpen(false)}
    />
    </>
  );
}
