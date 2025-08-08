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
  const [ledStatus, setLedStatus] = useState({
    white: false,
    red: false,
    yellow: false,
    blue: false
  });
  const [buzzerOn, setBuzzerOn] = useState(false);
  const [dht, setDht] = useState({ temperature: 0, humidity: 0 });

  useEffect(() => {
    const fetchStatus = async () => {
      try {
        const res = await axios.get('/api/status');
        const data = res.data;
        setDht({ temperature: data.temperature, humidity: data.humidity });
        setRfidList([data.lastCardID]);
        setLedStatus((prev) => ({
          ...prev,
          white: data.ledOn || false
        }));
      } catch (err) {
        console.error('Cihaz durumu alınamadı:', err);
      }
    };

    fetchStatus();
  }, []);

  const handleToggleLed = async (color) => {
    try {
      await axios.post('/api/control/led', { color });
      setLedStatus((prev) => ({
        ...prev,
        [color]: !prev[color]
      }));
    } catch (error) {
      console.error(`LED toggle error for ${color}:`, error);
    }
  };

  const handleToggleBuzzer = async () => {
    try {
      await axios.post('/api/control/buzzer');
      setBuzzerOn((prev) => !prev);
    } catch (error) {
      console.error('Buzzer toggle error:', error);
    }
  };

  const handleSendServo = async ({ servoId, angle, userId }) => {
    try {
      await axios.post('/api/servo-control', { servoId, angle, userId });
    } catch (error) {
      console.error('Servo control error:', error);
    }
  };

  const handleMeasureDHT = async () => {
    try {
      const response = await axios.get('/api/status');
      setDht({
        temperature: response.data.temperature,
        humidity: response.data.humidity
      });
    } catch (error) {
      console.error('DHT measure error:', error);
    }
  };

  return (
    <div className="panel-container">
      <h2>🧠 IoT Kontrol Paneli</h2>

      {/* RFID ve Kullanıcı Bilgileri Yan Yana */}
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

      <ServoPanel userId={user?.id} onSend={handleSendServo} />
      <BuzzerPanel buzzerOn={buzzerOn} onToggle={handleToggleBuzzer} />
      <LedPanel ledStatus={ledStatus} onToggle={handleToggleLed} />
      <SensorPanel
        temperature={dht.temperature}
        humidity={dht.humidity}
        onMeasure={handleMeasureDHT}
      />
    </div>
  );
}

export default DevicePanel;
