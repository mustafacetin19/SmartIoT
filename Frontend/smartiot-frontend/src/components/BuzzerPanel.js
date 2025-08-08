import React from 'react';
import axios from 'axios';
import './Panel.css';

function BuzzerPanel() {
  const handleBeep = (count) => {
    axios.post('http://localhost:8080/api/buzzer/beep', null, {
      params: { count }
    })
    .then(() => console.log(`✅ Buzzer ${count} kez çaldı`))
    .catch((error) => console.error(`❌ Buzzer hatası:`, error));
  };

  return (
    <div className="section-panel">
      <h3>🔊 Buzzer Control</h3>
      <div className="buzzer-button-group">
        <button onClick={() => handleBeep(1)}>🔈 Beep 1 Time</button>
        <button onClick={() => handleBeep(2)}>🔈 Beep 2 Time</button>
        <button onClick={() => handleBeep(3)}>🔈 Beep 3 Time</button>
      </div>
    </div>
  );
}

export default BuzzerPanel;
