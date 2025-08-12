// src/pages/RoomCreationPage.jsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "./DeviceSelectionPage.css"; // aynı görsel dil

export default function RoomCreationPage() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user"));
  const [roomName, setRoomName] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const submit = async (e) => {
    e.preventDefault();
    if (!roomName.trim()) return;
    try {
      setSaving(true); setError("");
      await axios.post("http://localhost:8080/api/user-rooms", {
        userId: user.id,
        roomName: roomName.trim(),
      });
      navigate("/select-device", { state: { refresh: true } });
    } catch (err) {
      setError(err?.response?.data?.message || "Oda oluşturulamadı.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <h1 className="title">➕ Yeni Oda Oluştur</h1>
      {error && <div className="alert error">{error}</div>}

      <form className="card center" onSubmit={submit} style={{ display: "grid", gap: 12 }}>
        <input
          className="input"
          placeholder="Oda adı (örn: Salon)"
          value={roomName}
          onChange={(e) => setRoomName(e.target.value)}
          required
        />
        <div className="quicklinks">
          <button className="btn primary" type="submit" disabled={saving}>
            {saving ? "Oluşturuluyor…" : "Oluştur"}
          </button>
          <button className="btn ghost" type="button" onClick={() => navigate("/select-device")}>
            Vazgeç
          </button>
        </div>
      </form>
    </div>
  );
}
