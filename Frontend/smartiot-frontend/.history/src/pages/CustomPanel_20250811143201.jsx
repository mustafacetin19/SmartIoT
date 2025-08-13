// src/pages/CustomPanel.jsx
import React, { useEffect, useState } from 'react';
import axios from 'axios';
import ServoPanel from '../components/ServoPanel';
import RfidPanel from '../components/RfidPanel';
import LedPanel from '../components/LedPanel';
import BuzzerPanel from '../components/BuzzerPanel';
import SensorPanel from '../components/SensorPanel';
import './CustomPanel.css';

const CustomPanel = () => {
  const [userDevices, setUserDevices] = useState([]);
  const [filteredDevices, setFilteredDevices] = useState([]);

  const [selectedRoom, setSelectedRoom] = useState('');
  const [selectedType, setSelectedType] = useState('');
  const [searchName, setSearchName] = useState('');

  const user = JSON.parse(localStorage.getItem('user'));
  const userId = user?.id;

  const getType = (d) => {
    const explicit = d?.device?.type;
    if (explicit) return explicit;
    const model = (d?.device?.deviceModel || '').toUpperCase();
    if (model.startsWith('LED')) return 'LED';
    if (model.startsWith('SERVO')) return 'SERVO';
    if (model.includes('RFID')) return 'RFID';
    if (model.includes('BUZZ')) return 'BUZZER';
    if (model.includes('DHT')) return 'DHT11';
    return 'Other';
  };
  const getRoomName = (d) => d?.userRoom?.roomName || '';

  useEffect(() => {
    if (!userId) return;
    axios.get(`http://localhost:8080/api/user-devices/user/${userId}`)
      .then(res => {
        const onlyAssignedActive = (res.data || [])
          .filter(d => d.active && d.userRoom);
        setUserDevices(onlyAssignedActive);
        setFilteredDevices(onlyAssignedActive);
      })
      .catch(err => console.error(err));
  }, [userId]);

  useEffect(() => {
    const filtered = userDevices.filter(device => {
      const roomName = getRoomName(device);
      const type = getType(device);
      const matchesRoom = selectedRoom === '' || roomName === selectedRoom;
      const matchesType = selectedType === '' || type === selectedType;
      const matchesName = (device.assignedName || '')
        .toLowerCase()
        .includes(searchName.toLowerCase());
      return matchesRoom && matchesType && matchesName;
    });
    setFilteredDevices(filtered);
  }, [selectedRoom, selectedType, searchName, userDevices]);

  const handleRemove = (deviceId) => {
    axios.put(`http://localhost:8080/api/user-devices/${deviceId}/deactivate`)
      .then(() => setUserDevices(prev => prev.filter(d => d.id !== deviceId)))
      .catch(err => console.error("Error while deactivating:", err));
  };

  const getUniqueRooms = () =>
    Array.from(new Set(userDevices.map(d => getRoomName(d)).filter(Boolean)))
      .sort((a,b)=>a.localeCompare(b,'tr'));

  const getUniqueTypes = () =>
    Array.from(new Set(userDevices.map(d => getType(d)).filter(Boolean)));

  const renderDevicePanel = (device) => {
    const type = getType(device);
    const panelProps = { device };
    let panel;
    switch (type) {
      case 'LED': panel = <LedPanel {...panelProps} />; break;
      case 'SERVO': panel = <ServoPanel {...panelProps} />; break;
      case 'RFID': panel = <RfidPanel {...panelProps} />; break;
      case 'BUZZER': panel = <BuzzerPanel {...panelProps} />; break;
      case 'DHT11': panel = <SensorPanel {...panelProps} />; break;
      default: panel = <div>❓ Unsupported device type: {type}</div>;
    }
    return (
      <>
        {panel}
        <button className="remove-button" onClick={() => handleRemove(device.id)}>❌ Remove</button>
      </>
    );
  };

  const groupedByType = filteredDevices.reduce((groups, device) => {
    const type = getType(device) || 'Other';
    if (!groups[type]) groups[type] = [];
    groups[type].push(device);
    return groups;
  }, {});

  const typeIcons = {
    LED: '💡 LED Devices',
    SERVO: '🛠️ Servo Devices',
    RFID: '📡 RFID Devices',
    BUZZER: '🔊 Buzzer Devices',
    DHT11: '🌡️ Sensor Devices',
    Other: '❓ Other Devices'
  };

  return (
    <div className="panel-container">
      <h2 className="title">🎛️ Your Personal IoT Panel</h2>

      <div className="filter-bar">
        <select value={selectedRoom} onChange={e => setSelectedRoom(e.target.value)}>
          <option value="">All Rooms</option>
          {getUniqueRooms().map(room => (
            <option key={room} value={room}>{room}</option>
          ))}
        </select>

        <select value={selectedType} onChange={e => setSelectedType(e.target.value)}>
          <option value="">All Types</option>
          {getUniqueTypes().map(type => (
            <option key={type} value={type}>{type}</option>
          ))}
        </select>

        <input
          type="text"
          placeholder="Search by Device Name"
          value={searchName}
          onChange={e => setSearchName(e.target.value)}
        />
      </div>

      {Object.entries(groupedByType).map(([type, devices]) => (
        <div key={type} className="device-category">
          <h3>{typeIcons[type] || type}</h3>
          <div className="device-category-group">
            {devices.map(device => (
              <div key={device.id} className="device-card">
                <h4>🔧 {device.assignedName}</h4>
                {renderDevicePanel(device)}
              </div>
            ))}
          </div>
        </div>
      ))}

      {filteredDevices.length === 0 && (
        <div style={{opacity:.8, marginTop:12}}>
          Henüz oda atanmış cihaz yok. Lütfen <strong>Select Device</strong> sayfasından cihazı
          bir odaya ekleyin.
        </div>
      )}
    </div>
  );
};

export default CustomPanel;
