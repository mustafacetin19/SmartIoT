import React, { useEffect, useState } from 'react';
import { api } from '../lib/http';

function RfidPanel({ device }) {
  const user = JSON.parse(localStorage.getItem('user'));
  const userId = user?.id;

  const unitId = device?.device?.id;
  const [lastCardId, setLastCardId] = useState('-');

  useEffect(() => {
    let mounted = true;

    const fetchCardId = async () => {
      try {
        if (userId && unitId) {
          const res = await api.get('/control/rfid/last', {
            params: { userId, deviceId: unitId }
          });
          if (mounted && res.status === 200) {
            setLastCardId(res.data?.cardId || '-');
            return;
          }
        }
        // Fallback (global status)
        const response = await api.get('/status');
        if (mounted) setLastCardId(response.data.lastCardID || "-");
      } catch (error) {
        console.error('Card ID could not be obtained:', error);
      }
    };

    fetchCardId();
    const interval = setInterval(fetchCardId, 5000);
    return () => { mounted = false; clearInterval(interval); };
  }, [userId, unitId]);

  return (
    <div className="section-panel">
      <h3>📡 RFID Information</h3>
      <p><strong>Last Read RFID ID:</strong> {lastCardId}</p>
    </div>
  );
}

export default RfidPanel;
