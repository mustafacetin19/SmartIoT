import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import './Navbar.css';

function Navbar() {
  const location = useLocation();
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem('user');
    if (stored) {
      setUser(JSON.parse(stored));
    } else {
      setUser(null);
    }
  }, [location]);

  const handleLogout = () => {
    localStorage.removeItem('user');
    setUser(null);
    navigate('/login');
  };

  const toggleDropdown = () => setDropdownOpen(!dropdownOpen);

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-logo">
        <span className="brand-bold">Smart</span>IoT
      </Link>

      <ul className="nav-links">
        <li>
          <Link to="/" className={location.pathname === '/' ? 'active' : ''}>
            SmartIoT
          </Link>
        </li>
        <li>
          <Link to="/about" className={location.pathname === '/about' ? 'active' : ''}>
            What is IoT?
          </Link>
        </li>
        <li>
          <Link to="/devices" className={location.pathname === '/devices' ? 'active' : ''}>
            Devices
          </Link>
        </li>

        {/* ✅ Personal IoT Menüsü */}
        <li className="dropdown">
          <span className="dropdown-title">Personal IoT</span>
          <ul className="dropdown-content">
            <li>
              <Link to="/demo-panel" className={location.pathname === '/demo-panel' ? 'active' : ''}>
                Test Panel
              </Link>
            </li>
            <li>
              <Link to="/custom-panel" className={location.pathname === '/custom-panel' ? 'active' : ''}>
                Custom IoT
              </Link>
            </li>
            <li>
              <Link to="/select-device" className={location.pathname === '/select-device' ? 'active' : ''}>
                Select Device
              </Link>
            </li>
          </ul>
        </li>

        {!user ? (
          <>
            <li><Link to="/login">Login</Link></li>
            <li><Link to="/register">Register</Link></li>
          </>
        ) : (
          <li className="user-menu" onClick={toggleDropdown}>
            <span className="user-name">
              {user.firstName} {user.lastName}
              <span className="dropdown-arrow">▾</span>
            </span>
            {dropdownOpen && (
              <div className="user-dropdown">
                <p><strong>ID:</strong> {user.id}</p>
                <p><strong>Email:</strong> {user.email}</p>
                <Link to="/user-settings" className="dropdown-link">⚙️ Account Settings</Link>
                <button className="logout-button-nav" onClick={handleLogout}>Logout</button>
              </div>
            )}
          </li>
        )}
      </ul>
    </nav>
  );
}

export default Navbar;
