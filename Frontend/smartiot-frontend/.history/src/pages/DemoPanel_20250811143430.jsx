// src/pages/DemoPanel.jsx
import React from 'react';
import ServoPanel from '../components/ServoPanel';
import RfidPanel from '../components/RfidPanel';
import LedPanel from '../components/LedPanel';
import BuzzerPanel from '../components/BuzzerPanel';
import SensorPanel from '../components/SensorPanel';

import './Devices.css';
import '../components/Buttons.css';
import './CustomPanel.css';        // 👈 başlık rengi & ortalama

const DemoPanel = () => {
  return (
    <div className="panel-container">
      <h2 className="title">🧪 IoT Test Panel</h2>
      <p className="demo-info" style={{textAlign:'center', opacity:.8, marginTop:-6, marginBottom:18}}>
        This page shows sample usage of supported IoT devices. It is not user-specific.
      </p>

      <div className="device-category">
        <h3 className="category-title">📡 RFID Device</h3>
        <div className="device-category-group">
          <div className="device-card">
            <h3>🔧 Sample RFID</h3>
            <RfidPanel onRemove={() => {}} />
          </div>
        </div>
      </div>

      <div className="device-category">
        <h3 className="category-title">💡 LED Device</h3>
        <div className="device-category-group">
          <div className="device-card">
            <h3>🔧 Sample LED</h3>
            <LedPanel onRemove={() => {}} />
          </div>
        </div>
      </div>

      <div className="device-category">
        <h3 className="category-title">⚙️ Servo Device</h3>
        <div className="device-category-group">
          <div className="device-card">
            <h3>🔧 Sample Servo</h3>
            <ServoPanel onRemove={() => {}} />
          </div>
        </div>
      </div>

      <div className="device-category">
        <h3 className="category-title">🔊 Buzzer Device</h3>
        <div className="device-category-group">
          <div className="device-card">
            <h3>🔧 Sample Buzzer</h3>
            <BuzzerPanel onRemove={() => {}} />
          </div>
        </div>
      </div>

      <div className="device-category">
        <h3 className="category-title">🌡️ Sensor Device</h3>
        <div className="device-category-group">
          <div className="device-card">
            <h3>🔧 Sample Sensor</h3>
            <SensorPanel onRemove={() => {}} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default DemoPanel;
