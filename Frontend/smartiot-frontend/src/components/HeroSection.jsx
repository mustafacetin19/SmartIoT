// src/components/HeroSection.jsx
import React from 'react';
import './HeroSection.css';
import { Link } from 'react-router-dom';
import bgImage from '../assets/images/hero-bg.jpg'; // ✅ Image import

function HeroSection() {
  return (
    <section
      className="hero-section"
      style={{ backgroundImage: `url(${bgImage})` }}
    >
      <div className="hero-overlay">
        <div className="hero-content">
          <h1>🔐 Welcome to the Smart IoT Control System</h1>
          <p>
            Control your ESP32-based devices in real-time, monitor data, and ensure secure access.
          </p>
          <div className="hero-buttons">
            <Link to="/login" className="btn btn-primary">Login</Link>
            <Link to="/register" className="btn btn-secondary">Register</Link>
            <Link to="/devices" className="btn btn-outline">Explore Devices</Link>
          </div>
        </div>
      </div>
    </section>
  );
}

export default HeroSection;
