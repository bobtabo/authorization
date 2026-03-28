"use client";

import React from "react";
import { ConsoleHeader } from "@/components/console-header";
import { ConsoleFooter } from "@/components/console-footer";

/** Notion 連携（OAuth / ワークスペース設定は API 接続後に拡張） */
export default function NotionIntegrationPage(): React.JSX.Element {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <ConsoleHeader />
      <main
        className="flex-1 max-w-3xl mx-auto px-6 py-10 w-full"
        data-testid="notion-integration-page"
      >
        <h1 className="text-xl font-semibold text-gray-900">Notion 連携</h1>
        <p className="mt-2 text-sm text-gray-600">
          ワークスペースとの連携設定をここで行います。
        </p>
      </main>
      <ConsoleFooter />
    </div>
  );
}
