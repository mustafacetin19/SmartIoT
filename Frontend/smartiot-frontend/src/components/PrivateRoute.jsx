// src/components/PrivateRoute.jsx
import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

const PrivateRoute = ({ children }) => {
  const user = JSON.parse(localStorage.getItem('user'));
  const location = useLocation();

  if (!user) {
    // gitmeden önceki sayfayı localStorage'a kaydet
    localStorage.setItem('redirectAfterLogin', location.pathname);
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default PrivateRoute;
