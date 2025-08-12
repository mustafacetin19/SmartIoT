import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './DeviceSelectionPage.css';
import '../components/Buttons.css';

const DeviceSelectionPage = () => {
  const [devices, setDevices] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [userDevices, setUserDevices] = useState([]);
  const [selectedDeviceId, setSelectedDeviceId] = useState('');
  const [selectedRoomId, setSelectedRoomId] = useState('');
  const [assignedName, setAssignedName] = useState('');
  const [message, setMessage] = useState('');
  const [user, setUser] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
      setMessage("⚠️ You must be logged in.");
      return;
    }

    const parsedUser = JSON.parse(storedUser);
    setUser(parsedUser);

    const fetchData = async () => {
      try {
        const [deviceRes, userDeviceRes, roomRes] = await Promise.all([
          axios.get('http://localhost:8080/api/device'),
          axios.get(`http://localhost:8080/api/user-devices/user/${parsedUser.id}`),
          axios.get(`http://localhost:8080/api/rooms`)  // ✅ Değiştirildi
        ]);

        setDevices(deviceRes.data.filter(d => d.active));
        setUserDevices(userDeviceRes.data);
        setRooms(roomRes.data);
      } catch (err) {
        console.error('Veriler alınamadı:', err);
        setMessage("❌ Sunucudan veri alınamadı.");
      }
    };

    fetchData();
  }, []);

  const handleAddDevice = async () => {
    if (!selectedDeviceId || !selectedRoomId || !assignedName) {
      setMessage("⚠️ Lütfen tüm alanları doldurun.");
      return;
    }

    try {
      const res = await axios.post('http://localhost:8080/api/user-devices', {
        user: { id: user.id },
        device: { id: parseInt(selectedDeviceId) },
        room: { id: parseInt(selectedRoomId) },
        assignedName: assignedName,
        active: true
      });

      setUserDevices(prev => [...prev, res.data]);
      setMessage(`✅ ${assignedName} added.`);
      setSelectedDeviceId('');
      setSelectedRoomId('');
      setAssignedName('');
    } catch (err) {
      console.error(err);
      setMessage(`❌ Failed to add ${assignedName}.`);
    }
  };

  return (
    <div className="panel-container">
      <h2>➕ Add Devices to Your Account</h2>
      {message && <p>{message}</p>}

      <div className="device-add-controls">
        {/* 1. Cihaz seçimi */}
        <select
          value={selectedDeviceId}
          onChange={(e) => setSelectedDeviceId(e.target.value)}
        >
          <option value="">📟 Select a device</option>
          {devices.map(device => (
            <option key={device.id} value={device.id}>
              {device.deviceName} - {device.deviceModel}
            </option>
          ))}
        </select>

        {/* 2. Oda seçimi */}
        <select
          value={selectedRoomId}
          onChange={(e) => setSelectedRoomId(e.target.value)}
        >
          <option value="">🏠 Select a room</option>
          {rooms.map(room => (
            <option key={room.id} value={room.id}>
              {room.name}
            </option>
          ))}
        </select>

        {/* 3. Atanacak isim */}
        <input
          type="text"
          placeholder="Assigned name"
          value={assignedName}
          onChange={(e) => setAssignedName(e.target.value)}
        />

        <button onClick={handleAddDevice} className="device-button add">
          ➕ Add
        </button>
      </div>
    </div>
  );
};

export default DeviceSelectionPage;
