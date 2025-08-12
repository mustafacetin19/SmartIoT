import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "./DeviceSelectionPage.css"; // aynı stil sınıflarını kullanıyoruz

export default function DeviceRegistrationPage() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user"));
  const [form, setForm] = useState({
    deviceModel: "",
    deviceName: "",
    deviceUid: "",
    alias: ""
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const handle = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    try {
      setSaving(true); setError("");
      await axios.post("http://localhost:8080/api/user-devices/register", {
        userId: user.id, ...form
      });
      navigate("/select-device", { state: { refreshDevices: true } });
    } catch (err) {
      setError(err?.response?.data?.message || "Kayıt başarısız.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <h1 className="title">Yeni Cihaz Oluştur</h1>
      {error && <div className="alert error">{error}</div>}
      <form className="card" onSubmit={submit} style={{ display: "grid", gap: 10 }}>
        <input className="input" name="deviceModel" placeholder="Model (örn: LED-White)" value={form.deviceModel} onChange={handle} required />
        <input className="input" name="deviceName"  placeholder="Ad (örn: White LED)" value={form.deviceName} onChange={handle} required />
        <input className="input" name="deviceUid"   placeholder="UID (örn: LED-001)" value={form.deviceUid} onChange={handle} required />
        <input className="input" name="alias"       placeholder="Takma ad (opsiyonel)" value={form.alias} onChange={handle} />
        <div className="actions">
          <button className="btn primary" type="submit" disabled={saving}>{saving ? "Kaydediliyor…" : "Kaydet"}</button>
          <button className="btn ghost" type="button" onClick={() => navigate("/select-device")}>İptal</button>
        </div>
      </form>
    </div>
  );
}
