// src/components/NavbarMinimal.jsx
import React from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';

const NavbarMinimal = () => {
  return (
    <nav className="navbar">
      <div className="logo">
        <Link to="/">SmartIoT</Link>
      </div>
    </nav>
  );
};

export default NavbarMinimal;
