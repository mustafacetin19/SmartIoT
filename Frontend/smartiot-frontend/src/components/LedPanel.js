import React, { useState } from 'react';
import axios from 'axios';
import './Panel.css';

function LedPanel() {
  const [ledStates, setLedStates] = useState({
    white: false,
    red: false,
    yellow: false,
    blue: false,
  });

  const handleLedToggle = (color) => {
    const newState = !ledStates[color];

    axios.post("http://localhost:8080/api/led/toggle", null, {
      params: { color, state: newState }
    })
    .then(() => {
      console.log(`✅ ${color} LED ${newState ? 'opened' : 'closed'}`);
      setLedStates(prev => ({ ...prev, [color]: newState }));
    })
    .catch((error) => {
      console.error(`❌ ${color} LED hatası:`, error);
    });
  };

  return (
    <div className="section-panel">
      <h3>💡 LED Control</h3>
      <div className="led-grid">
        {["white", "red", "yellow", "blue"].map((color) => (
          <button
            key={color}
            className={`led-button ${color}`}
            onClick={() => handleLedToggle(color)}
          >
            {color.toUpperCase()} {ledStates[color] ? "Close" : "Open"}
          </button>
        ))}
      </div>
    </div>
  );
}

export default LedPanel;
