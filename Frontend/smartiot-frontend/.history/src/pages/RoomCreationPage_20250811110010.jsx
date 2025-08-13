import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

export default function RoomCreationPage() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user")); // { id, ... }
  const [roomName, setRoomName] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!roomName.trim()) return;
    try {
      await axios.post("http://localhost:8080/api/user-rooms", {
        userId: user.id,
        roomName: roomName.trim(),
      });
      // Select-Device'a dön ve odaları yenile
      navigate("/select-device", { state: { refresh: true } });
    } catch (err) {
      alert(err?.response?.data?.message || "Oda oluşturulamadı.");
    }
  };

  return (
    <div className="container">
      <h2>Yeni Oda Oluştur</h2>
      <form onSubmit={handleSubmit}>
        <input
          placeholder="Oda adı (örn: Salon)"
          value={roomName}
          onChange={(e) => setRoomName(e.target.value)}
        />
        <div style={{ marginTop: 8, display: "flex", gap: 8 }}>
          <button type="submit">Oluştur</button>
          <button type="button" onClick={() => navigate("/select-device")}>Vazgeç</button>
        </div>
      </form>
    </div>
  );
}
