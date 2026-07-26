import { useEffect, useState } from "react";

import type { Pager as PagerData } from "@/shared/api/types";
import { extractApiError } from "@/shared/lib/api-error";
import { formatTimestamp } from "@/shared/lib/format-datetime";

import { getClient, updateClient, deleteClient, getJwtHistories, type JwtHistory } from "../api";
import { STATUS_MAP, type ClientDetail } from "../types";

function getStatusStyle(status: string) {
  switch (status) {
    case "利用中":
      return "bg-emerald-100 text-emerald-800 border border-emerald-200";
    case "アーカイブ":
      return "bg-slate-100 text-slate-600 border border-slate-200";
    case "停止中":
      return "bg-rose-100 text-rose-700 border border-rose-200";
    case "準備中":
      return "bg-amber-100 text-amber-700 border border-amber-200";
    default:
      return "bg-gray-100 text-gray-700 border border-gray-200";
  }
}

export function useClientDetail() {
  const [clientId, setClientId] = useState<number | null>(null);
  const [detail, setDetail] = useState<ClientDetail | null>(null);
  const [version, setVersion] = useState<number>(1);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [startOpen, setStartOpen] = useState(false);
  const [starting, setStarting] = useState(false);
  const [stopOpen, setStopOpen] = useState(false);
  const [stopping, setStopping] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const [jwtData, setJwtData] = useState<JwtHistory[]>([]);
  const [jwtPager, setJwtPager] = useState<PagerData | null>(null);
  const [activeTab, setActiveTab] = useState<"info" | "history">("info");
  const [historyPage, setHistoryPage] = useState(1);
  const [historyPageSize, setHistoryPageSize] = useState(10);

  const loadDetail = (id: number | string) => {
    return getClient(id).then((res) => {
      const d = res as Record<string, unknown>;
      setDetail({
        clientName: d.name as string,
        identifier: d.identifier as string ?? "",
        postalCode: d.post_code as string ?? "",
        prefecture: d.pref as string ?? "",
        city: d.city as string ?? "",
        street: d.address as string ?? "",
        building: d.building as string ?? "",
        tel: d.tel as string ?? "",
        email: d.email as string ?? "",
        status: STATUS_MAP[d.status as number] ?? "準備中",
        startedAt: formatTimestamp(d.start_at as string | null),
        stoppedAt: formatTimestamp(d.stop_at as string | null),
        createdAt: formatTimestamp(d.created_at as string),
        updatedAt: formatTimestamp(d.updated_at as string),
      });
      setVersion((d.version as number) ?? 1);
    });
  };

  useEffect(() => {
    const id = new URLSearchParams(window.location.search).get("id");
    if (!id) return;
    setClientId(Number(id));
    loadDetail(id);
    getJwtHistories(id, { page: 1, limit: 10 }).then((res) => {
      setJwtData(res.data);
      setJwtPager(res.pager);
    }).catch(() => {});
  }, []);

  useEffect(() => {
    if (!clientId) return;
    getJwtHistories(clientId, { page: historyPage, limit: historyPageSize }).then((res) => {
      setJwtData(res.data);
      setJwtPager(res.pager);
    }).catch(() => {});
  }, [clientId, historyPage, historyPageSize]);

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 3000);
    return () => clearTimeout(t);
  }, [toast]);

  const buildUpdatePayload = (status: number) => ({
    id: clientId,
    name: detail?.clientName,
    post_code: detail?.postalCode,
    pref: detail?.prefecture,
    city: detail?.city,
    address: detail?.street,
    building: detail?.building,
    tel: detail?.tel,
    email: detail?.email,
    status,
    version,
  });

  const handleDelete = () => {
    if (!clientId) return;
    setDeleting(true);
    deleteClient(clientId, { version }).then(() => {
      window.location.href = "/clients";
    }).catch((err: unknown) => {
      setDeleting(false);
      setDeleteOpen(false);
      setToast(extractApiError(err, "削除に失敗しました。"));
    });
  };

  const handleStart = () => {
    if (!clientId || !detail) return;
    setStarting(true);
    updateClient(clientId, buildUpdatePayload(2))
      .then(() => loadDetail(clientId))
      .then(() => {
        setStarting(false);
        setStartOpen(false);
        setToast("利用を開始しました。");
      }).catch((err: unknown) => {
        setStarting(false);
        setToast(extractApiError(err, "利用開始に失敗しました。"));
      });
  };

  const handleStop = () => {
    if (!clientId || !detail) return;
    setStopping(true);
    updateClient(clientId, buildUpdatePayload(3))
      .then(() => loadDetail(clientId))
      .then(() => {
        setStopping(false);
        setStopOpen(false);
        setToast("利用を停止しました。");
      }).catch((err: unknown) => {
        setStopping(false);
        setToast(extractApiError(err, "利用停止に失敗しました。"));
      });
  };

  return {
    detail,
    deleteOpen,
    setDeleteOpen,
    deleting,
    startOpen,
    setStartOpen,
    starting,
    stopOpen,
    setStopOpen,
    stopping,
    toast,
    jwtData,
    jwtPager,
    activeTab,
    setActiveTab,
    setHistoryPage,
    setHistoryPageSize,
    handleDelete,
    handleStart,
    handleStop,
    getStatusStyle,
  };
}
