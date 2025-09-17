import React, { useEffect, useState } from 'react';
import './UserSettingsPage.css';
import { useNavigate } from 'react-router-dom';

export default function UserSettingsPage() {
  const [user, setUser] = useState(null);
  const [err, setErr] = useState('');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  
  useEffect(() => {
    // localStorage'dan kullanıcıyı göster (backend'e bağlı kalmadan)
    const stored = localStorage.getItem('user');
    setUser(stored ? JSON.parse(stored) : null);
    setLoading(false);
  }, []);

  const handleDelete = async () => {
    if (!user) return;
    if (!window.confirm('Hesabını kalıcı olarak silmek istediğine emin misin?')) return;

    try {
      // Backend’inizde uygun endpoint varsa:
      const res = await fetch(`http://localhost:8080/api/users/${user.id}`, { method: 'DELETE' });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      localStorage.removeItem('user');
      navigate('/register');
    } catch (e) {
      setErr('Hesap silinemedi veya endpoint mevcut değil. (Detay: ' + (e?.message || 'bilinmiyor') + ')');
    }
  };

  if (loading) return <div className="page page-narrow"><div className="skeleton">Yükleniyor…</div></div>;

  return (
      <div className="page page-narrow">
        <h1 className="page-title">Account Settings</h1>

        {err && <div className="alert error">{err}</div>}

        {!user ? (
            <div className="card">
              <p>Oturum bulunamadı.</p>
              <button className="btn" onClick={() => navigate('/login')}>Login</button>
            </div>
        ) : (
            <div className="card">
              <div className="grid two">
                <div>
                  <label>ID</label>
                  <div className="value">{user.id}</div>
                </div>
                <div>
                  <label>Role</label>
                  <div className="value">{user.role || 'user'}</div>
                </div>
                <div>
                  <label>Ad Soyad</label>
                  <div className="value">{user.firstName} {user.lastName}</div>
                </div>
                <div>
                  <label>Email</label>
                  <div className="value">{user.email}</div>
                </div>
              </div>

              <div className="actions">
                <button className="btn danger" onClick={handleDelete}>Hesabı Sil</button>
                <button className="btn ghost" onClick={() => navigate('/')}>Geri</button>
              </div>
            </div>
        )}
      </div>
  );
}
