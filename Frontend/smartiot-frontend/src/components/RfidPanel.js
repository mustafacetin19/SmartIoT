import React, { useEffect, useState } from 'react';
import axios from 'axios';

function RfidPanel() {
  const [lastCardId, setLastCardId] = useState('Nothin');

  useEffect(() => {
    const fetchCardId = async () => {
      try {
        const response = await axios.get('/api/status');
        console.log("RFID API Response:", response.data); // Debug
        setLastCardId(response.data.lastCardID || "-");
      } catch (error) {
        console.error('Card ID could not be obtained:', error);
      }
    };

    fetchCardId();
    const interval = setInterval(fetchCardId, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="section-panel">
      <h3>📡 RFID Information</h3>
      <p><strong>Last Read RFID ID:</strong> {lastCardId}</p>
    </div>
  );
}

export default RfidPanel;
