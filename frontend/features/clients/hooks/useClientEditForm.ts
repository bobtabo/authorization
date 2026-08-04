import React, { useEffect, useRef, useState } from "react";

import { usePostcodeJpLookup } from "@/shared/hooks/use-postcode-jp-lookup";
import { extractApiError } from "@/shared/lib/api-error";
import { formatCityWard } from "@/shared/lib/postcode-jp";

import { getClient, updateClient } from "../api";

export function useClientEditForm() {
  const [clientId, setClientId] = useState<number | null>(null);
  const [clientName, setClientName] = useState<string>("");
  const [postalCode, setPostalCode] = useState<string>("");
  const [prefecture, setPrefecture] = useState<string>("");
  const [city, setCity] = useState<string>("");
  const [cityChoiceIndex, setCityChoiceIndex] = useState<number>(0);
  const [street, setStreet] = useState<string>("");
  const [building, setBuilding] = useState<string>("");
  const [tel, setTel] = useState<string>("");
  const [email, setEmail] = useState<string>("");
  const [version, setVersion] = useState<number>(1);
  const [saving, setSaving] = useState<boolean>(false);
  const [message, setMessage] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState<boolean>(false);
  const userChangedPostal = useRef(false);

  const handleTelChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const digitsOnly = e.target.value.replace(/\D/g, "");
    setTel(digitsOnly.slice(0, 255));
  };

  const handlePostalChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const v = e.target.value.replace(/\D/g, "");
    userChangedPostal.current = true;
    setPostalCode(v.slice(0, 7));
  };

  const { loading: postcodeLoading, error: postcodeError, rows: postcodeRows } =
    usePostcodeJpLookup(postalCode);

  useEffect(() => {
    const id = new URLSearchParams(window.location.search).get("id");
    const numericId = Number(id);
    if (!id || !Number.isInteger(numericId) || numericId <= 0) {
      setMessage("クライアントを特定できませんでした。");
      return;
    }
    setClientId(numericId);
    getClient(id).then((res) => {
      const d = res as Record<string, unknown>;
      setClientName(d.name as string ?? "");
      setPostalCode(d.post_code as string ?? "");
      setPrefecture(d.pref as string ?? "");
      setCity(d.city as string ?? "");
      setStreet(d.address as string ?? "");
      setBuilding(d.building as string ?? "");
      setTel(d.tel as string ?? "");
      setEmail(d.email as string ?? "");
      setVersion((d.version as number) ?? 1);
    }).catch((err: unknown) => {
      setMessage(extractApiError(err, "クライアントの取得に失敗しました。"));
    });
  }, []);

  useEffect(() => {
    if (postcodeRows.length === 0) {
      const digits = postalCode.replace(/\D/g, "");
      if (digits.length < 7 && userChangedPostal.current) {
        setPrefecture("");
        setCity("");
        setCityChoiceIndex(0);
      }
      return;
    }
    if (!userChangedPostal.current) return;
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
    if (!clientId) return;
    setSaving(true);
    setMessage(null);

    updateClient(clientId, {
      id: clientId,
      name: clientName,
      post_code: postalCode,
      pref: prefecture,
      city,
      address: street,
      building,
      tel,
      email,
      version,
    }).then(() => {
      setConfirmOpen(false);
      sessionStorage.setItem("flashMessage", "クライアントを更新しました。");
      window.location.href = "/clients";
    }).catch((err: unknown) => {
      setConfirmOpen(false);
      setMessage(extractApiError(err, "更新に失敗しました。"));
    }).finally(() => {
      setSaving(false);
    });
  };

  return {
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
  };
}
