// PATH: src/App.jsx
import React from 'react';
import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';

import Navbar from './components/Navbar';
import Footer from './components/Footer';
import PrivateRoute from './components/PrivateRoute';

import Home from './pages/Home';
import About from './pages/About';
import Devices from './pages/Devices';
import DeviceSelectionPage from './pages/DeviceSelectionPage';
import DemoPanel from './pages/DemoPanel';
import CustomPanel from './pages/CustomPanel';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import UserSettingsPage from './pages/UserSettingsPage';

// SCENES
import ScenesPage from './pages/ScenesPage';

function AppContent() {
  const location = useLocation();
  const hideLayoutRoutes = []; // gerekirse ekleyebilirsin
  const hideLayout = hideLayoutRoutes.includes(location.pathname);

  return (
    <>
      {!hideLayout && <Navbar />}

      <div className="app-container">
        <Routes>
          {/* Herkese açık sayfalar */}
          <Route path="/" element={<Home />} />
          <Route path="/about" element={<About />} />
          <Route path="/devices" element={<Devices />} />
          <Route path="/demo-panel" element={<DemoPanel />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Korumalı sayfalar */}
          <Route
            path="/select-device"
            element={
              <PrivateRoute>
                <DeviceSelectionPage />
              </PrivateRoute>
            }
          />
          <Route
            path="/custom-panel"
            element={
              <PrivateRoute>
                <CustomPanel />
              </PrivateRoute>
            }
          />
          <Route
            path="/scenes"
            element={
              <PrivateRoute>
                <ScenesPage />
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

          {/* 404 → Home */}
          <Route path="*" element={<Home />} />
        </Routes>
      </div>

      {!hideLayout && <Footer />}
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}
