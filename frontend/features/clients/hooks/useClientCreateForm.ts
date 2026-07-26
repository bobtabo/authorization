import React, { useEffect, useState } from "react";

import { usePostcodeJpLookup } from "@/shared/hooks/use-postcode-jp-lookup";
import { extractApiError } from "@/shared/lib/api-error";
import { formatCityWard } from "@/shared/lib/postcode-jp";

import { createClient } from "../api";

export function useClientCreateForm() {
  const [clientName, setClientName] = useState<string>("");
  const [postalCode, setPostalCode] = useState<string>("");
  const [prefecture, setPrefecture] = useState<string>("");
  const [city, setCity] = useState<string>("");
  /** rows.length > 1 のときの市区町村プルダウン選択インデックス */
  const [cityChoiceIndex, setCityChoiceIndex] = useState<number>(0);
  const [street, setStreet] = useState<string>("");
  const [building, setBuilding] = useState<string>("");
  const [tel, setTel] = useState<string>("");
  const [email, setEmail] = useState<string>("");
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

  const handleConfirmRegister = () => {
    setSaving(true);
    setMessage(null);

    createClient({
      name: clientName,
      post_code: postalCode,
      pref: prefecture,
      city,
      address: street,
      building,
      tel,
      email,
    }).then(() => {
      setConfirmOpen(false);
      sessionStorage.setItem("flashMessage", "クライアントを登録しました。");
      window.location.href = "/clients";
    }).catch((err: unknown) => {
      setConfirmOpen(false);
      setMessage(extractApiError(err, "登録に失敗しました。"));
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
    handleConfirmRegister,
  };
}
