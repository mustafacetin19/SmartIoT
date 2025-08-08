import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';
import './Form.css';
import circuitBackground from './circuit-bg.png';

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleLogin = async () => {
    try {
      const response = await axios.post('http://localhost:8080/api/login', {
        email,
        password,
      });

      if (response.status === 200) {
        localStorage.setItem('user', JSON.stringify(response.data));

        const redirectPath = localStorage.getItem('redirectAfterLogin') || '/';
        localStorage.removeItem('redirectAfterLogin');
        navigate(redirectPath);
      }
    } catch (error) {
      if (error.response) {
        if (error.response.status === 403) {
          setMessage('🚫 Your account is deactivated. Please register again.');
          setTimeout(() => navigate('/register'), 3000); // 3 sn sonra yönlendir
        } else if (error.response.status === 401) {
          setMessage('❌ Invalid email or password.');
        } else {
          setMessage('⚠️ Login failed due to server error.');
        }
      } else {
        setMessage('⚠️ Cannot connect to the server.');
      }

      setTimeout(() => setMessage(''), 5000);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleLogin();
    }
  };

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
