// src/App.jsx
import React from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';

import Home from './pages/Home';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import About from './pages/About';
import Devices from './pages/Devices';
import CustomPanel from './pages/CustomPanel';
import AddDevicePage from './pages/AddDevicePage';
import DeviceSelectionPage from './pages/DeviceSelectionPage';
import UserSettingsPage from './pages/UserSettingsPage';
import DemoPanel from './pages/DemoPanel';
import NotFound from './pages/NotFoundPage';

// ✅ yeni import
import RoomCreationPage from './pages/RoomCreationPage';

import Navbar from './components/Navbar';
import Footer from './components/Footer';
import PrivateRoute from './components/PrivateRoute';

function AppContent() {
  const location = useLocation();
  const hideLayoutRoutes = []; // gerekirse layout gizlenecek path'leri ekle
  const hideLayout = hideLayoutRoutes.includes(location.pathname);

  return (
    <>
      {!hideLayout && <Navbar />}
      <Routes>
        {/* 🔓 Herkese açık sayfalar */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/about" element={<About />} />
        <Route path="/devices" element={<Devices />} />
        <Route path="/demo-panel" element={<DemoPanel />} />

        {/* 🔒 Giriş gerektiren sayfalar */}
        <Route
          path="/custom-panel"
          element={
            <PrivateRoute>
              <CustomPanel />
            </PrivateRoute>
          }
        />
        <Route
          path="/add-device"
          element={
            <PrivateRoute>
              <AddDevicePage />
            </PrivateRoute>
          }
        />
        <Route
          path="/select-device"
          element={
            <PrivateRoute>
              <DeviceSelectionPage />
            </PrivateRoute>
          }
        />
        {/* ✅ yeni rota */}
        <Route
          path="/rooms/new"
          element={
            <PrivateRoute>
              <RoomCreationPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/user-settings"
          element={
            <PrivateRoute>
              <UserSettingsPage />
            </PrivateRoute>
          }
        />

        {/* 🚫 404 - Sayfa Bulunamadı */}
        <Route path="*" element={<NotFound />} />
      </Routes>
      {!hideLayout && <Footer />}
    </>
  );
}

export default function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}
