// src/pages/About.jsx
import React from 'react';
import './About.css';

function About() {
  return (
    <div className="about-container">
      <div className="about-card">
        <h2>💡 What is IoT (Internet of Things)?</h2>
        <p>
          IoT allows physical devices to connect to the internet and communicate with each other.
          These systems involve sensors, actuators, and communication protocols to collect,
          analyze, and control data remotely and efficiently.
        </p>
        <p>
          For example, temperature data can be used to trigger a microcontroller (like ESP32)
          to turn on a fan or rotate a servo motor. Using protocols like MQTT, these devices
          can be managed remotely in real-time.
        </p>
      </div>
    </div>
  );
}

export default About;
