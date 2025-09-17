// PATH: src/components/PrivateRoute.jsx
import React, { useEffect, useMemo } from "react";
import { Navigate, useLocation } from "react-router-dom";

// Basit auth helper: userId varsa veya 'user' objesi varsa oturum var kabul ederiz.
function hasSession() {
  const uid = localStorage.getItem("userId") || localStorage.getItem("id");
  const userRaw = localStorage.getItem("user");
  return Boolean(uid) || Boolean(userRaw);
}

/**
 * Korumalı route sarmalayıcısı.
 * - Oturum yoksa: /login'e yönlendirir ve redirectAfterLogin yazar.
 * - Oturum varsa: children'ı render eder.
 * - storage event ile reaktif: başka tabda logout olursa, bu tabda da koruma devreye girer.
 */
export default function PrivateRoute({ children }) {
  const location = useLocation();
  const authed = useMemo(() => hasSession(), []); // ilk mount kontrolü

  // Reaktif: storage değişirse yeniden kontrol
  const [ok, setOk] = React.useState(authed);
  useEffect(() => {
    const handler = () => setOk(hasSession());
    window.addEventListener("storage", handler);
    handler(); // ilk mount
    return () => window.removeEventListener("storage", handler);
  }, []);

  if (!ok) {
    // Kullanıcıyı login sonrası geri döndürmek için path'i sakla
    try {
      localStorage.setItem("redirectAfterLogin", location.pathname + location.search);
    } catch {}
    return <Navigate to="/login" replace />;
  }

  return children;
}
