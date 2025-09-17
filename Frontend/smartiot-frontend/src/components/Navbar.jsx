// PATH: src/components/Navbar.jsx
import React, { useEffect, useRef, useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import "./Navbar.css";
import { getCurrentUserName, clearSession } from "../api";

export default function Navbar() {
  const navigate = useNavigate();

  // Kullanıcı adı
  const [userName, setUserName] = useState(getCurrentUserName());

  // Dropdown kontrolü
  const [open, setOpen] = useState(false);
  const menuRef = useRef(null);
  const btnRef = useRef(null);

  // Storage değişince kullanıcı adını tazele
  useEffect(() => {
    const handler = () => setUserName(getCurrentUserName());
    window.addEventListener("storage", handler);
    handler(); // ilk mount
    return () => window.removeEventListener("storage", handler);
  }, []);

  // Dışarı tıklayınca & ESC ile dropdown kapat
  useEffect(() => {
    const onClickOutside = (e) => {
      if (!open) return;
      const clickedInside =
        menuRef.current?.contains(e.target) || btnRef.current?.contains(e.target);
      if (!clickedInside) setOpen(false);
    };
    const onKey = (e) => {
      if (e.key === "Escape") setOpen(false);
    };
    document.addEventListener("mousedown", onClickOutside);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onClickOutside);
      document.removeEventListener("keydown", onKey);
    };
  }, [open]);

  const logout = (e) => {
    e.preventDefault();
    clearSession();
    setOpen(false);
    navigate("/login", { replace: true });
  };

  return (
    <header className="nav">
      <div className="nav__inner">
        {/* sol: marka */}
        <Link to="/" className="nav__brand">
          <span className="nav__logoDot" />
          <span className="nav__brandText">SmartIoT</span>
        </Link>

        {/* orta: bağlantılar (wrap'lanır, her cihazda görünür) */}
        <nav className="nav__links" aria-label="Main">
          <NavLink to="/" className="nav__link">SmartIoT</NavLink>
          <NavLink to="/about" className="nav__link">What is IoT?</NavLink>
          <NavLink to="/devices" className="nav__link">Devices</NavLink>
          <NavLink to="/select-device" className="nav__link">Rooms &amp; Assign</NavLink>
          <NavLink to="/scenes" className="nav__link">Scenes</NavLink>
        </nav>

        {/* sağ: kullanıcı */}
        <div className="nav__user">
          <button
            ref={btnRef}
            type="button"
            className="nav__userBtn"
            aria-expanded={open}
            aria-haspopup="menu"
            onClick={() => setOpen((v) => !v)}
          >
            Hoş geldin, {userName || "Misafir"}
          </button>

          {open && (
            <div
              ref={menuRef}
              role="menu"
              className="nav__menu"
              aria-label="User"
            >
              <Link role="menuitem" to="/custom-panel" onClick={() => setOpen(false)}>
                Personal IoT
              </Link>
              <Link role="menuitem" to="/user-settings" onClick={() => setOpen(false)}>
                Account
              </Link>
              <a role="menuitem" href="/login" onClick={logout}>
                Logout
              </a>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
