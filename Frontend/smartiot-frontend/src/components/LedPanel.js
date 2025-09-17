import React, { useState } from 'react';
import { api } from '../lib/http';
import './Panel.css';

function LedPanel({ device }) {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = user?.id;

  const unitId = device?.device?.id;
  const title = device?.assignedName || 'LED';
  const [isOn, setIsOn] = useState(false);
  const [msg, setMsg] = useState('');

  const toggle = async () => {
    if (!userId || !unitId) {
      setMsg('⚠️ Bu bileşeni CustomPanel içindeki bir cihaza bağlayın.');
      return;
    }
    try {
      const next = !isOn;
      await api.post('/control/led', null, {
        params: { userId, deviceId: unitId, state: next }
      });
      setIsOn(next);
      setMsg(`✅ ${title} ${next ? 'açıldı' : 'kapandı'}.`);
    } catch (error) {
      console.error(error);
      setMsg('❌ Hata: ' + (error?.response?.data || 'komut gönderilemedi'));
    }
  };

  return (
    <div className="section-panel">
      <h3>💡 LED Control {title ? `– ${title}` : ''}</h3>
      {!unitId ? (
        <div className="hint">Bu bileşeni CustomPanel içindeki bir cihaza bağlayın.</div>
      ) : (
        <button className={`led-button ${isOn ? 'on' : 'off'}`} onClick={toggle}>
          {isOn ? 'Turn OFF' : 'Turn ON'}
        </button>
      )}
      {msg && <p className="led-status">{msg}</p>}
    </div>
  );
}

export default LedPanel;
