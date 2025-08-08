// src/components/DeviceCard.jsx
import React from 'react';
import { Link } from 'react-router-dom';
import './DeviceCard.css';

function DeviceCard({ title, path, icon }) {
  return (
    <Link to={path} className="device-card">
      <div className="device-icon">{icon}</div>
      <h3>{title}</h3>
    </Link>
  );
}

export default DeviceCard;