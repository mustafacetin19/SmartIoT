import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './Form.css';
import circuitBackground from './circuit-bg.png';
import { setSession } from '../api'; // ⬅ oturum anahtarlarını tek noktadan yazacağız

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  // NOT: mutlak http://localhost:8080 yerine relative /api/... kullan
  async function handleLogin() {
    setMessage('');
    try {
      const res = await fetch('/api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ email, password }),
      });

      if (!res.ok) {
        if (res.status === 403) {
          setMessage('🚫 Your account is deactivated. Please register again.');
          setTimeout(() => navigate('/register'), 3000);
          return;
        }
        if (res.status === 401) {
          setMessage('❌ Invalid email or password.');
          return;
        }
        // Sunucu metni varsa göster
        const text = await res.text().catch(() => '');
        throw new Error(text || `HTTP ${res.status}`);
      }

      const data = await res.json();

      // Eski kod uyumu için "user" objesini sakla
      try { localStorage.setItem('user', JSON.stringify(data)); } catch {}

      // Navbar'ın doğru isim göstermesi için güvenli setSession
      // data: { id, firstName, lastName, ... } (sende böyle görünüyor)
      setSession({
        id: data?.id ?? data?.userId,
        firstName: data?.firstName ?? data?.first_name,
        lastName: data?.lastName ?? data?.last_name,
        fullName: data?.fullName ?? data?.full_name,
        email: data?.email,
      });

      const redirectPath = localStorage.getItem('redirectAfterLogin') || '/';
      localStorage.removeItem('redirectAfterLogin');
      navigate(redirectPath, { replace: true });
    } catch (err) {
      // Network hatası vb.
      setMessage('⚠️ Cannot connect to the server.');
      setTimeout(() => setMessage(''), 5000);
    }
  }

  const handleKeyDown = (e) => { if (e.key === 'Enter') handleLogin(); };

  return (
    <div
      className="background-wrapper page-fade"
      style={{ backgroundImage: `url(${circuitBackground})` }}
    >
      <div className="form-container">
        <h2>Login</h2>
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button onClick={handleLogin}>Login</button>

        {message && <div className="error-box">{message}</div>}

        <p>
          Don’t have an account? <Link to="/register">Register</Link>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;
