import React, { useState, useEffect } from 'react';
import axios from 'axios';
import RfidPanel from './RfidPanel';
import ServoPanel from './ServoPanel';
import BuzzerPanel from './BuzzerPanel';
import LedPanel from './LedPanel';
import SensorPanel from './SensorPanel';

function DevicePanel() {
  const user = JSON.parse(localStorage.getItem('user'));

  const [rfidList, setRfidList] = useState([]);
  const [dht, setDht] = useState({ temperature: 0, humidity: 0 });

  useEffect(() => {
    const fetchStatus = async () => {
      try {
        const res = await axios.get('/api/status');
        const data = res.data;
        setDht({ temperature: data.temperature, humidity: data.humidity });
        setRfidList([data.lastCardID]);
      } catch (err) {
        console.error('Cihaz durumu alınamadı:', err);
      }
    };

    fetchStatus();
  }, []);

  return (
    <div className="panel-container">
      <h2>🧠 IoT Kontrol Paneli</h2>

      {/* RFID ve Kullanıcı Bilgileri */}
      <div className="rfid-user-wrapper">
        <RfidPanel rfidList={rfidList} />
        <div className="user-box">
          <p><strong>ID:</strong> {user?.id}</p>
          <p><strong>Name - Surname:</strong> {user?.firstName} {user?.lastName}</p>
          <button
            className="logout-button"
            onClick={() => {
              localStorage.removeItem('user');
              window.location.href = '/';
            }}
          >
            LogOut
          </button>
        </div>
      </div>

      {/* Bu sayfada spesifik cihaz bağlamı olmadığı için paneller pasif bilgilendirici modda görünür */}
      <ServoPanel />
      <BuzzerPanel />
      <LedPanel />
      <SensorPanel
        temperature={dht.temperature}
        humidity={dht.humidity}
      />
    </div>
  );
}

export default DevicePanel;
