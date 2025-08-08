// src/pages/DemoPanel.jsx
import React from 'react';
import ServoPanel from '../components/ServoPanel';
import RfidPanel from '../components/RfidPanel';
import LedPanel from '../components/LedPanel';
import BuzzerPanel from '../components/BuzzerPanel';
import SensorPanel from '../components/SensorPanel';

import './Devices.css';
import '../components/Buttons.css';
import './CustomPanel.css'; // ✅ Remove butonları için stil dosyası

const DemoPanel = () => {
  const renderDemoCard = (title, Component) => (
    <div className="device-card">
      <h3>🔧 {title}</h3>
      <Component onRemove={() => {}} />
      <button className="remove-button" disabled>
        ❌ Remove (Demo)
      </button>
    </div>
  );

  return (
    <div className="panel-container">
      <h2>🧪 IoT Test Panel</h2>
      <p className="demo-info">
        This page shows sample usage of supported IoT devices. It is not user-specific.
      </p>

      <div className="device-category">
        <h3 className="category-title">📡 RFID Device</h3>
        <div className="device-category-group">
          {renderDemoCard("Sample RFID", RfidPanel)}
        </div>
      </div>

      <div className="device-category">
        <h3 className="category-title">💡 LED Device</h3>
        <div className="device-category-group">
          {renderDemoCard("Sample LED", LedPanel)}
        </div>
      </div>

      <div className="device-category">
        <h3 className="category-title">⚙️ Servo Device</h3>
        <div className="device-category-group">
          {renderDemoCard("Sample Servo", ServoPanel)}
        </div>
      </div>

      <div className="device-category">
        <h3 className="category-title">🔊 Buzzer Device</h3>
        <div className="device-category-group">
          {renderDemoCard("Sample Buzzer", BuzzerPanel)}
        </div>
      </div>

      <div className="device-category">
        <h3 className="category-title">🌡️ Sensor Device</h3>
        <div className="device-category-group">
          {renderDemoCard("Sample Sensor", SensorPanel)}
        </div>
      </div>
    </div>
  );
};

export default DemoPanel;
