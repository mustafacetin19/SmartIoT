import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import './Panel.css';
import '../components/Buttons.css';

function SensorPanel({ device }) {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = user?.id;

  const unitId = device?.device?.id;
  const [temperature, setTemperature] = useState(null);
  const [humidity, setHumidity] = useState(null);
  const [dataHistory, setDataHistory] = useState([]);

  useEffect(() => {
    let mounted = true;

    const fetchSensorData = async () => {
      try {
        if (userId && unitId) {
          const res = await axios.get('http://localhost:8080/api/control/sensor/last', {
            params: { userId, deviceId: unitId }
          });
          if (mounted && res.status === 200) {
            const { temperature, humidity } = res.data;
            setTemperature(temperature);
            setHumidity(humidity);
            setDataHistory(prev => [...prev.slice(-9), { time: new Date().toLocaleTimeString(), temperature, humidity }]);
            return;
          }
        }
        // Fallback (global)
        const response = await axios.get("http://localhost:8080/api/sensor");
        const { temperature, humidity } = response.data;
        if (mounted) {
          setTemperature(temperature);
          setHumidity(humidity);
          setDataHistory(prev => [...prev.slice(-9), { time: new Date().toLocaleTimeString(), temperature, humidity }]);
        }
      } catch (error) {
        console.error("❌ Sensör verisi alınamadı:", error);
      }
    };

    fetchSensorData();
    const interval = setInterval(fetchSensorData, 5000);
    return () => { mounted = false; clearInterval(interval); };
  }, [userId, unitId]);

  return (
    <div className="section-panel">
      <h3>🌡️ Temperature & Humidity</h3>
      <p><strong>Instantaneous Temperature:</strong> {temperature ?? '---'}°C</p>
      <p><strong>Instantaneous Humidity:</strong> {humidity ?? '---'}%</p>

      <div style={{ width: '100%', height: 250, marginTop: '20px' }}>
        <ResponsiveContainer>
          <LineChart data={dataHistory}>
            <CartesianGrid strokeDasharray="3 3" stroke="#555" />
            <XAxis dataKey="time" stroke="#aaa" />
            <YAxis stroke="#aaa" />
            <Tooltip />
            <Legend />
            <Line type="monotone" dataKey="temperature" stroke="#ff9800" name="Temperature (°C)" />
            <Line type="monotone" dataKey="humidity" stroke="#2196f3" name="Humidity (%)" />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

export default SensorPanel;
