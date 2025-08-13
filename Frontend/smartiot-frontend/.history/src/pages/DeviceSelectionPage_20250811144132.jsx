// src/pages/DeviceSelectionPage.jsx
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import "./DeviceSelectionPage.css";

export default function DeviceSelectionPage() {
  const user = JSON.parse(localStorage.getItem("user"));
  const [devices, setDevices] = useState([]);   // tüm user_device kayıtları (atanmış + atanmamış)
  const [rooms, setRooms] = useState([]);

  const [selectedUserDeviceId, setSelectedUserDeviceId] = useState("");
  const [selectedRoomId, setSelectedRoomId] = useState("");
  const [assignedName, setAssignedName] = useState("");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const location = useLocation();
  const navigate = useNavigate();

  // ✅ Atanmış + atanmamış getir; etiket sadece devices tablosundan
  const loadDevices = async () => {
    const res = await axios.get(`http://localhost:8080/api/user-devices/user/${user.id}`);
    const rows = (res.data || [])
      .filter(ud => ud.active)
      .map(ud => {
        const model = ud.device?.deviceModel?.trim();
        const name  = ud.device?.deviceName?.trim();
        return {
          id: ud.id,                                  // user_devices.id
          label: model || name || `Device #${ud.device?.id}`,
          isAssigned: !!ud.userRoom
        };
      })
      .sort((a,b) => (a.isAssigned === b.isAssigned ? a.label.localeCompare(b.label, "tr") : a.isAssigned ? 1 : -1));

    setDevices(rows);
  };

  const loadRooms = async () => {
    const res = await axios.get(`http://localhost:8080/api/user-rooms/by-user/${user.id}`);
    setRooms(res.data || []);
  };

  const loadAll = async () => {
    try {
      setLoading(true); setError("");
      await Promise.all([loadDevices(), loadRooms()]);
    } catch (e) {
      setError(e?.response?.data?.message || "Veriler alınamadı.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadAll(); }, []);
  useEffect(() => {
    if (location.state?.refresh || location.state?.refreshDevices) {
      loadAll();
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location, navigate]);

  // Atama / yeniden atama
  const handleAssign = async () => {
    if (!selectedUserDeviceId) return alert("Cihaz seçiniz.");
    if (!assignedName.trim()) return alert("Cihaz ismi giriniz.");

    try {
      setSaving(true);
      await axios.put(`http://localhost:8080/api/user-devices/${selectedUserDeviceId}/assign`, {
        userId: user.id,
        userRoomId: selectedRoomId ? Number(selectedRoomId) : null,
        assignedName: assignedName.trim()
      });
      setSelectedUserDeviceId(""); setSelectedRoomId(""); setAssignedName("");
      await loadDevices();
    } catch (e) {
      alert(e?.response?.data?.message || "Atama başarısız.");
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
            {/* Cihaz */}
            <div className="field with-icon">
              <span className="icon">🧩</span>
              <select
                className="input"
                value={selectedUserDeviceId}
                onChange={(e) => setSelectedUserDeviceId(e.target.value)}
              >
                <option value="">Select a device</option>
                {devices.map((d) => (
                  <option key={d.id} value={d.id}>{d.label}</option>
                ))}
              </select>
            </div>

            {/* Oda */}
            <div className="field with-icon">
              <span className="icon">🏠</span>
              <select
                className="input"
                value={selectedRoomId}
                onChange={(e) => setSelectedRoomId(e.target.value)}
              >
                <option value="">Select a room</option>
                {rooms.map((r) => (
                  <option key={r.id} value={r.id}>{r.roomName}</option>
                ))}
              </select>
            </div>

            {/* İsim */}
            <div className="field">
              <input
                className="input"
                placeholder="Assigned name"
                value={assignedName}
                onChange={(e) => setAssignedName(e.target.value)}
              />
            </div>

            {/* Add */}
            <button
              className="btn primary"
              onClick={handleAssign}
              disabled={saving || !selectedUserDeviceId || !assignedName.trim()}
            >
              {saving ? "Adding…" : "Add"}
            </button>
          </div>

          <div className="quicklinks">
            <button className="btn secondary" onClick={() => navigate("/my-devices/new")}>+ Create device</button>
            <button className="btn secondary" onClick={() => navigate("/rooms/new")}>+ Create room</button>
            <button className="btn secondary" onClick={loadAll}>↻ Refresh</button>
          </div>
        </div>
      )}
    </div>
  );
}
