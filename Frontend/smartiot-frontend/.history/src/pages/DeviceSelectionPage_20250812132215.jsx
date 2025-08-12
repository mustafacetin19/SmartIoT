// src/pages/DeviceSelectionPage.jsx
import { useCallback, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import "./DeviceSelectionPage.css";

const API = "http://localhost:8080";

export default function DeviceSelectionPage() {
  const user = useMemo(() => JSON.parse(localStorage.getItem("user")), []);
  const userId = user?.id;

  const [pool, setPool] = useState([]);    // kullanıcının oda atanmamış cihazları (user_devices)
  const [rooms, setRooms] = useState([]);  // kullanıcının odaları

  const [selectedUserDeviceId, setSelectedUserDeviceId] = useState("");
  const [selectedRoomId, setSelectedRoomId] = useState("");
  const [assignedName, setAssignedName] = useState("");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const location = useLocation();
  const navigate = useNavigate();

  // --- data loaders (useCallback ile sabit referans) ---
  const loadPool = useCallback(async () => {
    if (!userId) return;
    const res = await axios.get(`${API}/api/user-devices/pool/${userId}`);
    const rows =
      (res.data || []).map((ud) => ({
        id: ud.id, // user_devices.id
        label:
          ud.assignedName?.trim() ||
          ud.device?.deviceModel ||
          ud.device?.deviceName ||
          `Device #${ud.device?.id}`,
      })) ?? [];
    setPool(rows);
  }, [userId]);

  const loadRooms = useCallback(async () => {
    if (!userId) return;
    const res = await axios.get(`${API}/api/user-rooms/by-user/${userId}`);
    setRooms(res.data || []);
  }, [userId]);

  const loadAll = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      await Promise.all([loadPool(), loadRooms()]);
    } catch (e) {
      setError(e?.response?.data?.message || "Veriler alınamadı.");
    } finally {
      setLoading(false);
    }
  }, [loadPool, loadRooms]);

  // ilk yükleme
  useEffect(() => {
    if (!userId) return;
    loadAll();
  }, [userId, loadAll]);

  // başka sayfadan "refresh" isteği gelirse
  useEffect(() => {
    if (location.state?.refresh || location.state?.refreshDevices) {
      loadAll();
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location, navigate, loadAll]);

  const resetForm = () => {
    setSelectedUserDeviceId("");
    setSelectedRoomId("");
    setAssignedName("");
  };

  const handleAssign = async () => {
    if (!selectedUserDeviceId) return alert("Önce bir cihaz seçin (havuz).");
    if (!selectedRoomId) return alert("Lütfen oda seçin.");
    if (!assignedName.trim()) return alert("Lütfen bir isim girin.");

    try {
      setSaving(true);
      await axios.put(`${API}/api/user-devices/${selectedUserDeviceId}/assign`, {
        userId,
        userRoomId: Number(selectedRoomId),
        assignedName: assignedName.trim(),
      });
      resetForm();
      await loadAll();
    } catch (e) {
      alert(e?.response?.data?.message || "Atama başarısız.");
    } finally {
      setSaving(false);
    }
  };

  // Kullanıcı yoksa login'e yönlendir
  useEffect(() => {
    if (!userId) {
      navigate("/login");
    }
  }, [userId, navigate]);

  return (
    <div className="page">
      <h1 className="title">➕ Add Devices to Your Account</h1>

      {error && <div className="alert error">{error}</div>}

      {loading ? (
        <div className="skeleton">Yükleniyor…</div>
      ) : (
        <div className="card center">
          <div className="inline-form">
            {/* HAVUZDAN cihaz seçimi (kullanıcıya özel) */}
            <div className="field with-icon">
              <span className="icon">🧩</span>
              <select
                className="input"
                value={selectedUserDeviceId}
                onChange={(e) => setSelectedUserDeviceId(e.target.value)}
              >
                <option value="">
                  {pool.length ? "Select a device" : "No devices yet (use Create device)"}
                </option>
                {pool.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Oda seçimi */}
            <div className="field with-icon">
              <span className="icon">🏠</span>
              <select
                className="input"
                value={selectedRoomId}
                onChange={(e) => setSelectedRoomId(e.target.value)}
              >
                <option value="">
                  {rooms.length ? "Select a room" : "No rooms yet (use Create room)"}
                </option>
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
                onClick={handleAssign}
                disabled={
                  saving ||
                  !selectedUserDeviceId ||
                  !selectedRoomId ||
                  !assignedName.trim()
                }
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
