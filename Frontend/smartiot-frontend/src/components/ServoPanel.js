import React, { useState } from 'react';
import { api } from '../lib/http';
import './Panel.css';

function ServoPanel({ device, defaultAngle = 90 }) {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = user?.id;

  const [angle, setAngle] = useState(String(defaultAngle));
  const [status, setStatus] = useState('');

  const unitId = device?.device?.id;
  const title = device?.assignedName || 'Servo';

  const handleSend = async (e) => {
    e?.preventDefault?.();
    if (!userId || !unitId) {
      setStatus('⚠️ Bu paneli CustomPanel içindeki bir cihaza bağlayın.');
      return;
    }
    try {
      await api.post('/control/servo', null, {
        params: { userId, deviceId: unitId, angle: Number(angle) }
      });
      setStatus('✅ Komut gönderildi.');
    } catch (error) {
      console.error(error);
      setStatus('❌ Hata: ' + (error?.response?.data || 'komut gönderilemedi'));
    }
  };

  return (
    <div className="section-panel">
      <h3>⚙️ Servo Motor Control {title ? `– ${title}` : ''}</h3>
      {!unitId ? (
        <div className="hint">Bu bileşeni CustomPanel içindeki bir cihaza bağlayın.</div>
      ) : (
        <form className="servo-form" onSubmit={handleSend}>
          <div className="servo-input-group">
            <label htmlFor={`servo-angle-${unitId}`}>Servo Angle (0 - 180°)</label>
            <input
              id={`servo-angle-${unitId}`}
              type="number"
              placeholder="90"
              min="0"
              max="180"
              value={angle}
              onChange={(e) => setAngle(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="servo-button">🛰️ Send</button>
        </form>
      )}
      {status && <p className="servo-status">{status}</p>}
    </div>
  );
}

export default ServoPanel;
