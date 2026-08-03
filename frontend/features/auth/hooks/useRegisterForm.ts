import React, { useState } from "react";

export function useRegisterForm() {
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

  return {
    email,
    setEmail,
    password,
    setPassword,
    showPassword,
    setShowPassword,
    loading,
    error,
    handleSubmit,
  };
}
