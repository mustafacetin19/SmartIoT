// src/pages/DeviceSelectionPage.jsx
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import "./DeviceSelectionPage.css";

export default function DeviceSelectionPage() {
  const user = JSON.parse(localStorage.getItem("user"));
  const userId = user?.id;

  const [catalog, setCatalog] = useState([]);   // devices (katalog)
  const [rooms, setRooms] = useState([]);       // user_room (aktif)
  const [deviceId, setDeviceId] = useState("");
  const [roomId, setRoomId] = useState("");
  const [assignedName, setAssignedName] = useState("");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const location = useLocation();
  const navigate = useNavigate();

  const loadCatalog = async () => {
    // aktif cihazları çekiyoruz; istersen /api/devices de olur
    const res = await axios.get("http://localhost:8080/api/devices/active");
    setCatalog(res.data || []);
  };

  const loadRooms = async () => {
    const res = await axios.get(`http://localhost:8080/api/user-rooms/by-user/${userId}`);
    setRooms(res.data || []);
  };

  const loadAll = async () => {
    try {
      setLoading(true);
      setError("");
      await Promise.all([loadCatalog(), loadRooms()]);
    } catch (e) {
      setError(e?.response?.data?.message || "Veriler alınamadı.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadAll(); }, [userId]);

  useEffect(() => {
    if (location.state?.refresh || location.state?.refreshDevices) {
      loadAll();
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location, navigate]);

  const handleAdd = async () => {
    if (!deviceId) return alert("Lütfen cihaz seçin.");
    if (!roomId) return alert("Lütfen oda seçin.");
    if (!assignedName.trim()) return alert("Lütfen bir isim girin.");

    try {
      setSaving(true);
      await axios.post("http://localhost:8080/api/user-devices/add", {
        userId: userId,
        deviceId: Number(deviceId),     // devices.id
        userRoomId: Number(roomId),     // user_room.id
        assignedName: assignedName.trim()
      });

      // reset + istersen bir toast göster
      setAssignedName("");
      setDeviceId("");
      setRoomId("");
    } catch (e) {
      alert(e?.response?.data?.message || "Ekleme başarısız.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <h1 className="title">➕ Add Devices to Your Account</h1>

      {error && <div className="alert error">{error}</div>}

      {loading ? (
        <div className="skeleton">Yükleniyor…</div>
      ) : (
        <div className="card center">
          <div className="inline-form">
            {/* Katalogtan cihaz */}
            <div className="field with-icon">
              <span className="icon">🧩</span>
              <select
                className="input"
                value={deviceId}
                onChange={(e) => setDeviceId(e.target.value)}
              >
                <option value="">Select a device</option>
                {catalog.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.deviceModel || d.deviceName} {/* ör: SERVO-2 */}
                  </option>
                ))}
              </select>
            </div>

            {/* Oda seçimi */}
            <div className="field with-icon">
              <span className="icon">🏠</span>
              <select
                className="input"
                value={roomId}
                onChange={(e) => setRoomId(e.target.value)}
              >
                <option value="">Select a room</option>
                {rooms.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.roomName}
                  </option>
                ))}
              </select>
            </div>

            {/* İsim + Add */}
            <div className="input-with-button">
              <input
                className="input"
                placeholder="Assigned name"
                value={assignedName}
                onChange={(e) => setAssignedName(e.target.value)}
              />
              <button
                className="btn primary"
                onClick={handleAdd}
                disabled={saving || !deviceId || !roomId || !assignedName.trim()}
                title="Add device to selected room"
              >
                {saving ? "Adding…" : "Add"}
              </button>
            </div>
          </div>

          <div className="quicklinks">
            <button className="btn action" onClick={() => navigate("/my-devices/new")}>
              + Create device
            </button>
            <button className="btn action" onClick={() => navigate("/rooms/new")}>
              + Create room
            </button>
            <button className="btn action" onClick={loadAll}>
              ↻ Refresh
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
