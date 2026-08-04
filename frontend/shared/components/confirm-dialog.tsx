"use client";

import { AnimatePresence, motion } from "framer-motion";
import { X } from "lucide-react";
import React, { useEffect, useRef } from "react";

type Props = {
  open: boolean;
  titleId: string;
  title: string;
  onClose: () => void;
  closeDisabled?: boolean;
  children: React.ReactNode;
};

/** 確認モーダル共通シェル。role="dialog"・Escapeでの閉じる・開いた時のフォーカス移動を提供する。 */
export function ConfirmDialog({
  open,
  titleId,
  title,
  onClose,
  closeDisabled = false,
  children,
}: Props): React.JSX.Element {
  const dialogRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    dialogRef.current?.focus();

    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && !closeDisabled) onClose();
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [open, closeDisabled, onClose]);

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
          onClick={() => !closeDisabled && onClose()}
        >
          <motion.div
            ref={dialogRef}
            initial={{ scale: 0.95, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0.95, opacity: 0 }}
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby={titleId}
            tabIndex={-1}
            className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4 outline-none"
          >
            <div className="flex items-center justify-between mb-4">
              <h2 id={titleId} className="text-lg font-semibold text-gray-900">
                {title}
              </h2>
              <button
                type="button"
                onClick={() => !closeDisabled && onClose()}
                className="text-gray-400 hover:text-gray-600"
                aria-label="閉じる"
              >
                <X size={20} />
              </button>
            </div>

            {children}
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
