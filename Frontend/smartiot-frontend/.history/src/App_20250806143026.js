// src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import Home from './pages/Home';
import CustomPanel from './pages/CustomPanel'; // Kullanıcı paneli (/custom-panel)
import DemoPanel from './pages/DemoPanel'; // Yeni oluşturacağımız panel

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/custom-panel" element={<CustomPanel />} />
        <Route path="/demo-panel" element={<DemoPanel />} /> {/* ✅ herkese açık demo */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Router>
  );
}

export default App;
