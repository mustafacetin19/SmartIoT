import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";

export default function DeviceSelectionPage() {
  const user = JSON.parse(localStorage.getItem("user")); // {id,...}
  const [devices, setDevices] = useState([]);
  const [rooms, setRooms] = useState([]);

  const [selectedDeviceId, setSelectedDeviceId] = useState("");
  const [selectedRoomId, setSelectedRoomId] = useState("");
  const [assignedName, setAssignedName] = useState("");

  const location = useLocation();
  const navigate = useNavigate();

  const loadDevices = async () => {
    const res = await axios.get("http://localhost:8080/api/devices");
    setDevices(res.data || []);
  };

  const loadRooms = async () => {
    const res = await axios.get(`http://localhost:8080/api/user-rooms/by-user/${user.id}`);
    setRooms(res.data || []);
  };

  useEffect(() => {
    loadDevices();
    loadRooms();
  }, []);

  // Oda oluşturma sayfasından dönüşte refresh
  useEffect(() => {
    if (location.state?.refresh) {
      loadRooms();
      navigate(location.pathname, { replace: true, state: {} });
    }
  }, [location, navigate]);

  const handleAdd = async () => {
    if (!selectedDeviceId) return alert("Cihaz seçiniz.");
    if (!assignedName.trim()) return alert("İsim giriniz.");

    try {
      await axios.post("http://localhost:8080/api/user-devices", {
        userId: user.id,
        deviceId: Number(selectedDeviceId),
        userRoomId: selectedRoomId ? Number(selectedRoomId) : null, // oda seçimi opsiyonel
        assignedName: assignedName.trim(),
      });
      alert("Cihaz eklendi.");
      setSelectedDeviceId("");
      setSelectedRoomId("");
      setAssignedName("");
    } catch (e) {
      alert(e?.response?.data?.message || "Cihaz ekleme başarısız.");
    }
  };

  return (
    <div className="container">
      <h2>Cihaz Ekle</h2>

      <label>Cihaz</label>
      <select value={selectedDeviceId} onChange={(e) => setSelectedDeviceId(e.target.value)}>
        <option value="">Seçiniz</option>
        {devices.map((d) => (
          <option key={d.id} value={d.id}>
            {d.device_name || d.deviceModel || `Device #${d.id}`}
          </option>
        ))}
      </select>

      <label>Oda (kullanıcıya özel)</label>
      <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
        <select value={selectedRoomId} onChange={(e) => setSelectedRoomId(e.target.value)}>
          <option value="">(Oda seçme)</option>
          {rooms.map((r) => (
            <option key={r.id} value={r.id}>
              {r.roomName}
            </option>
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
        <button onClick={handleAdd}>Add</button>
      </div>
    </div>
  );
}
