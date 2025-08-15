import React, { useState } from 'react';
import axios from 'axios';
import './Panel.css';

function BuzzerPanel({ device }) {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = user?.id;

  const unitId = device?.device?.id;
  const title = device?.assignedName || 'Buzzer';
  const [msg, setMsg] = useState('');

  const beep = async (times = 1) => {
    if (!userId || !unitId) {
      setMsg('⚠️ Bu bileşeni CustomPanel içindeki bir cihaza bağlayın.');
      return;
    }
    try {
      await axios.post('http://localhost:8080/api/control/buzzer', null, {
        params: { userId, deviceId: unitId, action: times === 1 ? 'beep' : `beep${times}` }
      });
      setMsg(`✅ ${title} ${times} kez çaldı.`);
    } catch (error) {
      console.error(error);
      setMsg('❌ Hata: ' + (error?.response?.data || 'komut gönderilemedi'));
    }
  };

  return (
    <div className="section-panel">
      <h3>🔊 Buzzer Control {title ? `– ${title}` : ''}</h3>
      {!unitId ? (
        <div className="hint">Bu bileşeni CustomPanel içindeki bir cihaza bağlayın.</div>
      ) : (
        <div className="buzzer-button-group">
          <button onClick={() => beep(1)}>🔈 Beep 1</button>
          <button onClick={() => beep(2)}>🔈 Beep 2</button>
          <button onClick={() => beep(3)}>🔈 Beep 3</button>
        </div>
      )}
      {msg && <p className="buzzer-status">{msg}</p>}
    </div>
  );
}

export default BuzzerPanel;
