import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import './Panel.css';
import '../components/Buttons.css'; // Hata veren yoldu, doğru konumdaysa bu şekilde bırakabilirsin

function SensorPanel({ onRemove }) {
  const [temperature, setTemperature] = useState(null);
  const [humidity, setHumidity] = useState(null);
  const [dataHistory, setDataHistory] = useState([]);

  useEffect(() => {
    const fetchSensorData = async () => {
      try {
        const response = await axios.get("http://localhost:8080/api/sensor");
        const { temperature, humidity } = response.data;

        setTemperature(temperature);
        setHumidity(humidity);

        setDataHistory(prev => [
          ...prev.slice(-9),
          { time: new Date().toLocaleTimeString(), temperature, humidity }
        ]);
      } catch (error) {
        console.error("❌ Sensör verisi alınamadı:", error);
      }
    };

    fetchSensorData();
    const interval = setInterval(fetchSensorData, 5000);

    return () => clearInterval(interval);
  }, []);

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
