// src/pages/UserSettingsPage.jsx
import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './UserSettingsPage.css';

const UserSettingsPage = () => {
  const [user, setUser] = useState(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
  }, []);

  const handleDeactivate = async () => {
    if (!user) return;

    try {
      const response = await axios.put(`http://localhost:8080/api/users/${user.id}/deactivate`);
      if (response.status === 200) {
        localStorage.removeItem('user');
        window.location.href = "/login"; // logout ve yönlendirme
      }
    } catch (err) {
      console.error(err);
      setMessage("❌ Failed to deactivate account.");
    }
  };

  if (!user) {
    return <div className="panel-container">Loading user info...</div>;
  }

  return (
    <div className="panel-container user-settings-page">
      <h2>⚙️ Account Settings</h2>

      <div className="user-info">
        <p><strong>👤 First Name:</strong> {user.firstName}</p>
        <p><strong>🧑‍💼 Last Name:</strong> {user.lastName}</p>
        <p><strong>📧 Email:</strong> {user.email}</p>
        <p><strong>🆔 ID:</strong> {user.id}</p>
      </div>

      <div className="settings-buttons">
        <button className="btn btn-deactivate" onClick={handleDeactivate}>
          <i className="material-icons"></i> Deactivate My Account
        </button>
      </div>

      {message && <p className="error-msg">{message}</p>}
    </div>
  );
};

export default UserSettingsPage;
