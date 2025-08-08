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

  useEffect(() => {
    if (!userId) return;

    axios.get(`http://localhost:8080/api/user-devices/user/${userId}`)
      .then(res => {
        const onlyActive = res.data.filter(d => d.active);
        setUserDevices(onlyActive);
        setFilteredDevices(onlyActive);
      })
      .catch(err => console.error(err));
  }, [userId]);

  useEffect(() => {
    const filtered = userDevices.filter(device => {
      const matchesRoom = selectedRoom === '' || device.room?.name === selectedRoom;
      const matchesType = selectedType === '' || device.device?.type === selectedType;
      const matchesName = device.assignedName?.toLowerCase().includes(searchName.toLowerCase());
      return matchesRoom && matchesType && matchesName;
    });
    setFilteredDevices(filtered);
  }, [selectedRoom, selectedType, searchName, userDevices]);

  const handleRemove = (deviceId) => {
    axios.put(`http://localhost:8080/api/user-devices/${deviceId}/deactivate`)
      .then(() => {
        const updatedDevices = userDevices.filter(d => d.id !== deviceId);
        setUserDevices(updatedDevices);
      })
      .catch(err => console.error("Error while deactivating:", err));
  };

  const getUniqueRooms = () => [...new Set(userDevices.map(d => d.room?.name).filter(Boolean))];
  const getUniqueTypes = () => [...new Set(userDevices.map(d => d.device?.type).filter(Boolean))];

  const renderDevicePanel = (device) => {
    const type = device.device?.type;
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
    const type = device.device?.type || 'Other';
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
      <h2>🎛️ Your Personal IoT Panel</h2>

      {/* 🔍 Filter Area */}
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

      {/* 🎛 Grouped Devices */}
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
    </div>
  );
};

export default CustomPanel;
