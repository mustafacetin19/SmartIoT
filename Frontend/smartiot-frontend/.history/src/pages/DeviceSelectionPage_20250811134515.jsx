import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import "./DeviceSelectionPage.css";


export default function DeviceSelectionPage() {
  const user = JSON.parse(localStorage.getItem("user"));
  const [pool, setPool] = useState([]);     // userDevice havuzu
  const [rooms, setRooms] = useState([]);

  const [selectedUserDeviceId, setSelectedUserDeviceId] = useState("");
  const [selectedRoomId, setSelectedRoomId] = useState("");
  const [assignedName, setAssignedName] = useState("");

  const location = useLocation();
  const navigate = useNavigate();

  const loadPool = async () => {
    const res = await axios.get(`http://localhost:8080/api/user-devices/pool/${user.id}`);
    const rows = (res.data || []).map(ud => ({
      id: ud.id,
      label: `${ud.device.deviceName} (${ud.device.deviceUid})${ud.assignedName ? " - " + ud.assignedName : ""}`
    }));
    setPool(rows);
  };

  const loadRooms = async () => {
    const res = await axios.get(`http://localhost:8080/api/user-rooms/by-user/${user.id}`);
    setRooms(res.data || []);
  };

  useEffect(() => { loadPool(); loadRooms(); }, []);

  useEffect(() => {
    if (location.state?.refresh || location.state?.refreshDevices) {
      loadRooms();
      loadPool();
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location, navigate]);

  const handleAssign = async () => {
    if (!selectedUserDeviceId) return alert("Cihaz seçiniz.");
    if (!assignedName.trim()) return alert("İsim giriniz.");
    try {
      await axios.put(`http://localhost:8080/api/user-devices/${selectedUserDeviceId}/assign`, {
        userId: user.id,
        userRoomId: selectedRoomId ? Number(selectedRoomId) : null,
        assignedName: assignedName.trim()
      });
      alert("Cihaz atandı.");
      setSelectedUserDeviceId("");
      setSelectedRoomId("");
      setAssignedName("");
      loadPool(); // atanmış olan havuzdan düşer
    } catch (e) {
      alert(e?.response?.data?.message || "Atama başarısız.");
    }
  };

  return (
    <div className="container">
      <h2>Cihaz Ekle</h2>

      {/* Combobox #1: Kullanıcı havuzu */}
      <label>Cihaz</label>
      <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
        <select value={selectedUserDeviceId} onChange={(e) => setSelectedUserDeviceId(e.target.value)}>
          <option value="">(Cihaz seç)</option>
          {pool.map((d) => (
            <option key={d.id} value={d.id}>{d.label}</option>
          ))}
        </select>
        <button onClick={() => navigate("/my-devices/new")}>+ Cihaz Oluştur</button>
      </div>

      {/* Combobox #2: Oda */}
      <label>Oda (kullanıcıya özel)</label>
      <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
        <select value={selectedRoomId} onChange={(e) => setSelectedRoomId(e.target.value)}>
          <option value="">(Oda seç)</option>
          {rooms.map((r) => (
            <option key={r.id} value={r.id}>{r.roomName}</option>
          ))}
        </select>
        <button onClick={() => navigate("/rooms/new")}>+ Oda Oluştur</button>
      </div>

      <label>Cihaz İsmi</label>
      <input
        placeholder="örn: Salon Beyaz Led"
        value={assignedName}
        onChange={(e) => setAssignedName(e.target.value)}
      />

      <div style={{ marginTop: 12 }}>
        <button onClick={handleAssign}>Add</button>
      </div>
    </div>
  );
}
