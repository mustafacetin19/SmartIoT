import React, { useState } from 'react';
import axios from 'axios';
import './Form.css'; // Stil dosyan varsa kullan

const AddDevicePage = () => {
  const [device, setDevice] = useState({
    deviceUid: '',
    deviceName: '',
    deviceModel: '',
  });
  const [message, setMessage] = useState('');

  const handleChange = (e) => {
    setDevice({ ...device, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8080/api/devices', device);
      setMessage('✅ Device successfully added!');
      setDevice({ deviceUid: '', deviceName: '', deviceModel: '' });
    } catch (err) {
      console.error(err);
      setMessage('❌ Failed to add device.');
    }
  };

  return (
    <div className="form-container">
      <h2>➕ Add New Device</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          name="deviceUid"
          placeholder="Device UID (unique)"
          value={device.deviceUid}
          onChange={handleChange}
          required
        />
        <input
          type="text"
          name="deviceName"
          placeholder="Device Name"
          value={device.deviceName}
          onChange={handleChange}
          required
        />
        <input
          type="text"
          name="deviceModel"
          placeholder="Device Model (e.g., LED, Servo)"
          value={device.deviceModel}
          onChange={handleChange}
          required
        />
        <button type="submit">Add Device</button>
      </form>
      {message && <p>{message}</p>}
    </div>
  );
};

export default AddDevicePage;
