import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import "./DeviceSelectionPage.css";

export default function DeviceSelectionPage() {
  const user = JSON.parse(localStorage.getItem("user"));
  const [pool, setPool] = useState([]);      // oda atanmamış user_device’lar
  const [rooms, setRooms] = useState([]);

  const [selectedUserDeviceId, setSelectedUserDeviceId] = useState("");
  const [selectedRoomId, setSelectedRoomId] = useState("");
  const [assignedName, setAssignedName] = useState("");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const location = useLocation();
  const navigate = useNavigate();

  const loadPool = async () => {
    const res = await axios.get(`http://localhost:8080/api/user-devices/pool/${user.id}`);
    const rows = (res.data || []).map(ud => ({
      id: ud.id,
      label: `${ud.device.deviceName} (${ud.device.deviceUid})${ud.assignedName ? " • " + ud.assignedName : ""}`,
    }));
    setPool(rows);
  };

  const loadRooms = async () => {
    const res = await axios.get(`http://localhost:8080/api/user-rooms/by-user/${user.id}`);
    setRooms(res.data || []);
  };

  const loadAll = async () => {
    try {
      setLoading(true);
      setError("");
      await Promise.all([loadPool(), loadRooms()]);
    } catch (e) {
      setError(e?.response?.data?.message || "Veriler alınırken bir hata oluştu.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadAll(); /* mount */ }, []);
  useEffect(() => {
    if (location.state?.refresh || location.state?.refreshDevices) {
      loadAll();
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location, navigate]);

  const handleAssign = async () => {
    if (!selectedUserDeviceId) return alert("Cihaz seçiniz.");
    if (!assignedName.trim()) return alert("Cihaz ismi giriniz.");
    try {
      setSaving(true);
      await axios.put(`http://localhost:8080/api/user-devices/${selectedUserDeviceId}/assign`, {
        userId: user.id,
        userRoomId: selectedRoomId ? Number(selectedRoomId) : null,
        assignedName: assignedName.trim(),
      });
      // başarılı: formu sıfırla & havuzu yenile
      setSelectedUserDeviceId(""); setSelectedRoomId(""); setAssignedName("");
      await loadPool();
    } catch (e) {
      alert(e?.response?.data?.message || "Cihaz ekleme başarısız.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <h1 className="title">Cihaz Ekle</h1>

      {error && <div className="alert error">{error}</div>}
      {loading ? (
        <div className="skeleton">Yükleniyor…</div>
      ) : (
        <div className="grid">
          {/* Sol Kart – Seçimler */}
          <section className="card">
            <div className="step">
              <div className="step-number">1</div>
              <div className="step-body">
                <label className="label">Cihaz</label>
                {pool.length === 0 ? (
                  <div className="empty">
                    Henüz cihaz eklememişsiniz.
                    <button className="btn link" onClick={() => navigate("/my-devices/new")}>
                      + Cihaz Oluştur
                    </button>
                  </div>
                ) : (
                  <div className="row gap">
                    <select
                      className="input"
                      value={selectedUserDeviceId}
                      onChange={(e) => setSelectedUserDeviceId(e.target.value)}
                    >
                      <option value="">(Cihaz seç)</option>
                      {pool.map((d) => (
                        <option key={d.id} value={d.id}>{d.label}</option>
                      ))}
                    </select>
                    <button className="btn ghost" onClick={() => navigate("/my-devices/new")}>
                      + Cihaz Oluştur
                    </button>
                  </div>
                )}
              </div>
            </div>

            <div className="step">
              <div className="step-number">2</div>
              <div className="step-body">
                <label className="label">Oda (kullanıcıya özel)</label>
                {rooms.length === 0 ? (
                  <div className="empty">
                    Henüz oda yok.
                    <button className="btn link" onClick={() => navigate("/rooms/new")}>
                      + Oda Oluştur
                    </button>
                  </div>
                ) : (
                  <div className="row gap">
                    <select
                      className="input"
                      value={selectedRoomId}
                      onChange={(e) => setSelectedRoomId(e.target.value)}
                    >
                      <option value="">(Oda seç)</option>
                      {rooms.map((r) => (
                        <option key={r.id} value={r.id}>{r.roomName}</option>
                      ))}
                    </select>
                    <button className="btn ghost" onClick={() => navigate("/rooms/new")}>
                      + Oda Oluştur
                    </button>
                  </div>
                )}
              </div>
            </div>

            <div className="step">
              <div className="step-number">3</div>
              <div className="step-body">
                <label className="label">Cihaz İsmi</label>
                <input
                  className="input"
                  placeholder="örn: Salon Beyaz Led"
                  value={assignedName}
                  onChange={(e) => setAssignedName(e.target.value)}
                />
              </div>
            </div>

            <div className="actions">
              <button
                className="btn primary"
                onClick={handleAssign}
                disabled={saving || !selectedUserDeviceId || !assignedName.trim()}
              >
                {saving ? "Ekleniyor…" : "Add"}
              </button>
              <button className="btn ghost" onClick={loadAll} disabled={saving}>Yenile</button>
            </div>
          </section>

          {/* Sağ Kart – İpucu / Özet */}
          <aside className="card muted">
            <h3 className="card-title">İpucu</h3>
            <ul className="tips">
              <li>Önce <strong>Cihaz Oluştur</strong> ile cihazınızı kaydedin.</li>
              <li>Cihaz listesinde görünmüyorsa, UID bilgisini kontrol edin.</li>
              <li>Oda listesinde kendinize özel odalarınız yer alır.</li>
            </ul>
            <div className="meta">
              <div>Havuzdaki cihaz: <strong>{pool.length}</strong></div>
              <div>Oda sayısı: <strong>{rooms.length}</strong></div>
            </div>
          </aside>
        </div>
      )}
    </div>
  );
}
