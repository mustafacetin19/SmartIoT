import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

export default function DeviceRegistrationPage() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user"));
  const [form, setForm] = useState({
    deviceModel: "",
    deviceName: "",
    deviceUid: "",
    alias: ""
  });

  const handle = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    try {
      await axios.post("http://localhost:8080/api/user-devices/register", {
        userId: user.id,
        ...form
      });
      alert("Cihaz kaydedildi.");
      navigate("/select-device", { state: { refreshDevices: true } });
    } catch (err) {
      alert(err?.response?.data?.message || "Kayıt başarısız.");
    }
  };

  return (
    <div className="container">
      <h2>Yeni Cihaz Oluştur</h2>
      <form onSubmit={submit} style={{ display: "grid", gap: 8, maxWidth: 420 }}>
        <input name="deviceModel" placeholder="Model (örn: LED-White)" value={form.deviceModel} onChange={handle} />
        <input name="deviceName"  placeholder="Ad (örn: White LED)" value={form.deviceName} onChange={handle} />
        <input name="deviceUid"   placeholder="UID (örn: LED-001)" value={form.deviceUid} onChange={handle} />
        <input name="alias"       placeholder="Takma ad (opsiyonel)" value={form.alias} onChange={handle} />
        <div style={{ display: "flex", gap: 8 }}>
          <button type="submit">Kaydet</button>
          <button type="button" onClick={() => navigate("/select-device")}>İptal</button>
        </div>
      </form>
    </div>
  );
}
