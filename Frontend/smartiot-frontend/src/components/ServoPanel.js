import React, { useState } from 'react';
import axios from 'axios';
import './Panel.css';

function ServoPanel() {
  const [servoId, setServoId] = useState('');
  const [angle, setAngle] = useState('');
  const [status, setStatus] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8080/api/servo-control', {
        servo: `servo${servoId}`,
        angle: Number(angle),
        userId: "1"
      });
      setStatus('✅ Komut başarıyla gönderildi!');
    } catch (error) {
      console.error(error);
      setStatus('❌ Hata oluştu!');
    }
  };

  return (
    <div className="section-panel">
      <h3>⚙️ Servo Motor Control</h3>
      <form className="servo-form" onSubmit={handleSubmit}>
        <div className="servo-input-group">
          <label htmlFor="servo-id">Servo ID (Exp: 1 or 2)</label>
          <input
            id="servo-id"
            type="number"
            placeholder="1"
            value={servoId}
            onChange={(e) => setServoId(e.target.value)}
            required
          />
        </div>

        <div className="servo-input-group">
          <label htmlFor="servo-angle">Servo Angel (0 - 180°)</label>
          <input
            id="servo-angle"
            type="number"
            placeholder="90"
            min="0"
            max="180"
            value={angle}
            onChange={(e) => setAngle(e.target.value)}
            required
          />
        </div>

        <button type="submit" className="servo-button">🛰️ Send Command</button>
      </form>
      {status && <p className="servo-status">{status}</p>}
    </div>
  );
}

export default ServoPanel;
