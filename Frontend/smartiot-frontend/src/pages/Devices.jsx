// src/pages/Devices.jsx
import React from 'react';
import './Devices.css';

function Devices() {
  const deviceList = [
    { icon: '🧠', name: 'ESP32 Microcontroller' },
    { icon: '💳', name: 'RFID Card Reader' },
    { icon: '🌡️', name: 'DHT11 Temperature & Humidity Sensor' },
    { icon: '⚙️', name: 'Servo Motors' },
    { icon: '💡', name: 'RGB LED Indicators' },
    { icon: '🔊', name: 'Buzzer Alert System' },
  ];

  return (
    <div className="devices-container">
      <div className="devices-card">
        <h2>🛠️ Supported Devices</h2>
        <ul>
          {deviceList.map((device, index) => (
            <li key={index} className="device-item">
              <span className="device-icon">{device.icon}</span>
              <span className="device-name">{device.name}</span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export default Devices;
